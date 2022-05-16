package correlation;

import ij.ImagePlus;
import ij.process.FHT;
import ij.process.FloatProcessor;
import java.util.concurrent.Future;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Arnold Fertin
 */
public class PhaseCorrelator2D
{
    public static final double DBL_EPSILON = 2.2204460492503131E-16;

    private final FHT fht1;

    private final FHT fht2;

    private final FHT correlation;

    private final int width;

    private final int height;

    private final Window win;

    public PhaseCorrelator2D(final ImagePlus img1,
                             final ImagePlus img2,
                             final WindowType type)
    {
        fht1 = new FHT(img1.duplicate().getProcessor());
        fht1.setShowProgress(false);
        fht2 = new FHT(img2.duplicate().getProcessor());
        fht2.setShowProgress(false);
        width = img1.getWidth();
        height = img2.getHeight();
        correlation = new FHT(new FloatProcessor(width, height));
        correlation.setShowProgress(false);
        win = new Window(width, height, type);
    }

    public final ImagePlus correlate()
    {
        win.apodize(fht1);
        fht1.transform();
        win.apodize(fht2);
        fht2.transform();
        final int nthreads = Math.min(ConcurrencyUtils.getNumberOfThreads(), height);
        final Future<?>[] futures = new Future<?>[nthreads];
        final int k = height / nthreads;
        for (int j = 0; j < nthreads; j++)
        {
            final int first = j * k;
            final int last;
            if (j == nthreads - 1)
            {
                last = height;
            }
            else
            {
                last = first + k;
            }
            futures[j] = ConcurrencyUtils.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    for (int y = first; y < last; y++)
                    {
                        int ySym = height - y;
                        if (ySym > height - 1)
                        {
                            ySym = 0;
                        }
                        for (int x = 0; x < width; x++)
                        {
                            int xSym = width - x;
                            if (xSym > width - 1)
                            {
                                xSym = 0;
                            }
                            final double p1 = fht1.getf(x, y);
                            final double p2 = fht1.getf(xSym, ySym);
                            final double q1 = fht2.getf(x, y);
                            final double q2 = fht2.getf(xSym, ySym);
                            double norm = Math.sqrt(p2 * p2 + p1 * p1) * Math.sqrt(q2 * q2 + q1 * q1);
                            if (norm == 0)
                            {
                                norm = DBL_EPSILON;
                            }
                            final double c = ((p2 - p1) * q2 + (p2 + p1) * q1) / norm;
                            correlation.setf(x, y, (float) c);
                        }
                    }
                }
            });
        }
        ConcurrencyUtils.waitForCompletion(futures);
        correlation.inverseTransform();
        correlation.swapQuadrants();

        return new ImagePlus("Correlation", correlation);
    }

    public final double[] getTranslation()
    {
        double maxCorr = Double.MIN_VALUE;
        int xMax = 0, yMax = 0;
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                final double v = correlation.getf(x, y);
                if (v > maxCorr)
                {
                    maxCorr = v;
                    xMax = x;
                    yMax = y;
                }
            }
        }
        final double u = xMax - width / 2 + getDeltaX(xMax, yMax, maxCorr);
        final double v = yMax - height / 2 + getDeltaY(xMax, yMax, maxCorr);
        return new double[]
        {
            u, v, maxCorr
        };
    }

    private double getDeltaX(final int xMax,
                             final int yMax,
                             final double maxCorr)
    {
        if (xMax - 1 < 0)
        {
            return 0;
        }
        if (xMax + 1 > width)
        {
            return 0;
        }
        final double cX1 = correlation.getf(xMax - 1, yMax);
        final double cX2 = correlation.getf(xMax + 1, yMax);
        return getDelta(cX1, cX2, maxCorr);
    }

    private double getDeltaY(final int xMax,
                             final int yMax,
                             final double maxCorr)
    {
        if (yMax - 1 < 0)
        {
            return 0;
        }
        if (yMax + 1 > height)
        {
            return 0;
        }
        final double cY1 = correlation.getf(xMax, yMax - 1);
        final double cY2 = correlation.getf(xMax, yMax + 1);
        return getDelta(cY1, cY2, maxCorr);
    }

    private double getDelta(final double cNeg,
                            final double cPos,
                            final double maxCorr)
    {
        if (cNeg > cPos)
        {
            if (cNeg < 0)
            {
                return 0;
            }
            return -cNeg / (cNeg + maxCorr); // -c1 / (c1 + c0)
        }
        if (cPos > cNeg)
        {
            if (cPos < 0)
            {
                return 0;
            }
            return cPos / (cPos + maxCorr); // c1 / (c1 + c0)
        }
        return 0;
    }
}
