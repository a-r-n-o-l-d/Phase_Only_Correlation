/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package correlation;

import ij.process.FloatProcessor;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.util.concurrent.Future;

/**
 *
 * @author Arnold Fertin
 */
public class Window
{
    private static final double HAMMING_C1 = 0.53836;

    private static final double HAMMING_C2 = 0.46164;

    private static final double HALF = 0.5;

    private static final double TUKEY_ALPHA = 0.5;

    private final double[] coef;

    private final int width;

    private final int height;

    public Window(final int width,
                  final int height,
                  final WindowType type)
    {
        this.width = width;
        this.height = height;
        coef = new double[width * height];
        for (int y = 0; y < height; y++)
        {
            final double wy = getWeight(y, height, type);
            for (int x = 0; x < width; x++)
            {
                coef[y * width + x] = getWeight(x, width, type) * wy;
            }
        }
    }

    public void apodize(final FloatProcessor img)
    {
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
                        for (int x = 0; x < width; x++)
                        {
                            final float v = img.getf(x, y) * getCoefficient(x, y);
                            img.setf(x, y, v);
                            //IJ.log("" + v);
                        }
                    }
                }
            });
        }
        ConcurrencyUtils.waitForCompletion(futures);
    }

    public float getCoefficient(final int x,
                                final int y)
    {
        return (float) coef[x + y * width];
    }

    private double getWeight(final int n,
                             final int size,
                             final WindowType type)
    {
        switch (type)
        {
            case COSINE:
                return cosine(n, size);
            case HAMMING:
                return hamming(n, size);
            case HANN:
                return hann(n, size);
            case TUKEY:
                return tukey(n, size);
            case WELCH:
                return welch(n, size);
            default:
                return 1;
        }
    }

    private double cosine(final double n,
                          final double size)
    {
        return sin(PI * n / (size - 1));
    }

    private double hamming(final double n,
                           final double size)
    {
        return HAMMING_C1 - HAMMING_C2 * cos(2 * PI * n / (size - 1));
    }

    private double hann(final double n,
                        final double size)
    {
        return HALF * (1 - cos(2 * PI * n / (size - 1)));
    }

    private double tukey(final double n,
                         final double size)
    {
        final double alpha = TUKEY_ALPHA;
        if (n >= 0 && n <= alpha * (size - 1) / 2)
        {
            return HALF * (1 + cos(PI * (2 * n / (alpha * (size - 1)) - 1)));
        }
        if (n > alpha * (size - 1) / 2 && n <= (size - 1) * (1 - alpha / 2))
        {
            return 1;
        }
        if (n > (size - 1) * (1 - alpha / 2) && n <= (size - 1))
        {
            return HALF * (1 + cos(PI * (2 * n / (alpha * (size - 1)) - 2 / alpha + 1)));
        }
        return 0;
    }

    private double welch(final double n,
                         final double size)
    {
        final double v = (n - (size - 1) / 2) / ((size - 1) / 2);
        return 1 - v * v;
    }
}
