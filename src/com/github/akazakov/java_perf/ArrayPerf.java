package com.github.akazakov.java_perf;

import org.openjdk.jmh.annotations.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Fork(1)
public class ArrayPerf {
    int MB = 1024*1024;
    int size = 1000 * MB;
    int intSize = size / Integer.BYTES;
    int[] arr = new int[intSize];
    byte[] byteArr = new byte[size];
    ByteBuffer byteBuffer = ByteBuffer.wrap(byteArr);
    IntBuffer byteBufAsIntBuf = byteBuffer.asIntBuffer();
    IntBuffer nativeIntBuf = IntBuffer.wrap(arr);

    @Benchmark
    public int[] allocateArray() {
        return new int[intSize];
    }

    @Benchmark
    public ByteBuffer allocateByteBuffer() {
        return ByteBuffer.allocate(size);
    }

    @Benchmark
    public ByteBuffer allocateDirectByteBuffer() {
        return ByteBuffer.allocateDirect(size);
    }

    @Benchmark
    public int[] initalizeArray() {
        for (var i = 0; i < intSize; i++) {
            arr[i] = i;
        }
        return arr;
    }

    @Benchmark
    public IntBuffer initalizeIntBuffer() {
        for (var i = 0; i < intSize; i++) {
            byteBufAsIntBuf.put(i, i);
        }
        return byteBufAsIntBuf;
    }

    @Benchmark
    public int sumByteBufAsIntBuffer() {
        var sum = 0;
        for (var i = 0; i < intSize; i++) {
            sum += byteBufAsIntBuf.get(i);
        }
        return sum;
    }

    @Benchmark
    public int sumIntBuffer() {
        var sum = 0;
        for (var i = 0; i < intSize; i++) {
            sum += nativeIntBuf.get(i);
        }
        return sum;
    }

    @Benchmark
    public int sumArray() {
        var sum = 0;
        for (var i = 0; i < intSize; i++) {
            sum += arr[i];
        }
        return sum;
    }
}
