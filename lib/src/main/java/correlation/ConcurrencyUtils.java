package correlation;

/*
 * Copyright (C) 2015 Arnold Fertin
 *
 * Centre National de la Recherche Scientifique
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */


import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 *
 * @author Arnold Fertin
 */
public final class ConcurrencyUtils
{
    private ConcurrencyUtils()
    {
    }

    private static ExecutorService threadPool
             = Executors.newCachedThreadPool(new CustomThreadFactory(new CustomExceptionHandler()));

    private static class CustomExceptionHandler implements Thread.UncaughtExceptionHandler
    {
        public void uncaughtException(final Thread t, final Throwable e)
        {
            e.printStackTrace();
        }
    }

    private static class CustomThreadFactory implements ThreadFactory
    {
        private static final ThreadFactory DEFAULT_FACTORY = Executors.defaultThreadFactory();

        private final Thread.UncaughtExceptionHandler handler;

        CustomThreadFactory(final Thread.UncaughtExceptionHandler handler)
        {
            this.handler = handler;
        }

        @Override
        public Thread newThread(final Runnable r)
        {
            final Thread t = DEFAULT_FACTORY.newThread(r);
            t.setUncaughtExceptionHandler(handler);
            t.setDaemon(true);
            return t;
        }
    };

    private static int nTnreads = getNumberOfProcessors();

    /**
     * Returns the number of available processors.
     * <p>
     * @return number of available processors
     */
    public static int getNumberOfProcessors()
    {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Returns the current number of threads.
     * <p>
     * @return the current number of threads.
     */
    public static int getNumberOfThreads()
    {
        return nTnreads;
    }
    
    /**
     * Sets the number of threads.
     * <p>
     * @param n the number of threads.
     */
    public static void setNumberOfThreads(final int n)
    {
        if (n < 1)
        {
            throw new IllegalArgumentException("n must be greater or equal 1");
        }
        nTnreads = n;
    }
    
    /**
     * Submits a Runnable task for execution and returns a Future representing
     * that task.
     * <p>
     * @param task task for execution
     * <p>
     * @return a handle to the task submitted for execution
     */
    public static Future<?> submit(final Runnable task)
    {
        if (threadPool.isShutdown() || threadPool.isTerminated())
        {
            threadPool =
               Executors.newCachedThreadPool(new CustomThreadFactory(new CustomExceptionHandler()));
        }
        return threadPool.submit(task);
    }
    
    /**
     * Waits for all threads to complete computation.
     * <p>
     * @param futures handles to running threads
     */
    public static void waitForCompletion(final Future<?>[] futures)
    {
        final int size = futures.length;
        try
        {
            for (int j = 0; j < size; j++)
            {
                futures[j].get();
            }
        }
        catch (ExecutionException ex)
        {
            ex.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
