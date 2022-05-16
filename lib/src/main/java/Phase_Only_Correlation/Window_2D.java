package Phase_Only_Correlation;

import correlation.Window;
import correlation.WindowType;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Arnold Fertin
 */
public class Window_2D implements PlugIn
{
    private ImagePlus img1;

    private WindowType type;

    @Override
    public void run(String string)
    {
        if (!doDialog())
        {
            return;
        }
        final int width = img1.getWidth();
        final int height = img1.getHeight();
        final Window win = new Window(width, height, type);
        IJ.run(img1, "32-bit", "");
        win.apodize((FloatProcessor) img1.getProcessor());
        img1.updateAndDraw();
    }

    private boolean doDialog()
    {
        final String[] images = WindowManager.getImageTitles();
        final GenericDialog gd = new GenericDialog("Phase Correlation", IJ.getInstance());
        gd.addChoice("Image", images, images[0]);
        gd.addChoice("Window_type", WindowType.NAMES, WindowType.NAMES[0]);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return false;
        }
        img1 = WindowManager.getImage(gd.getNextChoice());
        type = WindowType.valueOf(gd.getNextChoice());
        return true;
    }
}
