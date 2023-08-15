package com.aki.modfix.chunk.openGL;

import com.aki.modfix.Modfix;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class ChunkGLDispatcher {
    private final ExecutorService executor = new ForkJoinPool(Math.max(Runtime.getRuntime().availableProcessors() - 2, 1),
            pool -> new ForkJoinWorkerThread(pool) {
            }, (thread, exception) -> Minecraft.getMinecraft().crashed(new CrashReport("Chunk Compile Thread crashed.", exception)), true);
    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private final Thread thread = Thread.currentThread();

    public void update() {
        Runnable task;
        while ((task = taskQueue.poll()) != null) {
            task.run();
        }
    }

    public <T> CompletableFuture<T> runAsync(Supplier<T> supplier) {
        return crashMinecraftOnError(CompletableFuture.supplyAsync(supplier, executor));
    }

    public <T> CompletableFuture<T> runOnRenderThread(Supplier<T> supplier) {
        if (Thread.currentThread() == this.thread) {
            return CompletableFuture.completedFuture(supplier.get());
        } else {
            return CompletableFuture.supplyAsync(supplier, this.taskQueue::add);
        }
    }

    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return crashMinecraftOnError(CompletableFuture.runAsync(runnable, executor));
    }

    public CompletableFuture<Void> runOnRenderThread(Runnable runnable) {
        if (Thread.currentThread() == this.thread) {
            runnable.run();
            return CompletableFuture.completedFuture(null);
        } else {
            return CompletableFuture.runAsync(runnable, this.taskQueue::add);
        }
    }

    private static <T> CompletableFuture<T> crashMinecraftOnError(CompletableFuture<T> future) {
        return future.whenCompleteAsync((r, t) -> {
            if (t != null) {
                Minecraft.getMinecraft().crashed(new CrashReport("Failed compiling chunk", t));
            }
        }, ForkJoinPool.commonPool());
    }

    public void Remove_ShutDown() {
        executor.shutdown();
        this.update();
        try {
            if (!executor.awaitTermination(10_000, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(10_000, TimeUnit.MILLISECONDS))
                    Modfix.logger.error("ChunkRenderDispatcher did not terminate!");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        this.update();
    }
}
