package com.github.akazakov.java_perf;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Measurement(iterations = 10, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Fork(0)
public class ReadWritePerf {

    int MB = 1024*1024;
    int size = 2000 * MB;
    Path dir = Paths.get("/tmp/java_perf");
    Path writePath;
    Path readPath;
    long fSize;

    byte[] byteArray = new byte[size];
    ByteBuffer normalByteBuffer = ByteBuffer.allocate(size);
    ByteBuffer directByteBuffer = ByteBuffer.allocateDirect(size);
    FileChannel fc;

    @Setup
    public void setup() {
        for (int i = 0; i < size; i++) {
            byteArray[i] = (byte)i;
        }
        try {
            Files.createDirectories(dir);
            readPath = Files.createTempFile(dir, "read_file", "tmp");
            writePath = dir.resolve("test123");
            Files.write(readPath, byteArray);
            fSize = Files.size(readPath);
            fc = FileChannel.open(readPath, StandardOpenOption.READ);
        } catch (IOException e) {
            System.out.println("Exception!" + e.getMessage());
        }
        System.out.println("Done construction! SIZE = " + size / MB + "Mb");
    }

    @TearDown
    public void tearDown() throws IOException {
        fc.close();
    }

    @Benchmark
    public Path writeFileNio() throws IOException {
        return Files.write(writePath, byteArray);
    }

    @Benchmark
    public int readAllBytes() throws IOException {
        return Files.readAllBytes(readPath).length;
    }

    @Benchmark
    public int readNioStream() throws IOException {
        var is = Files.newInputStream(readPath);
        return is.read(byteArray);
    }

    @Benchmark
    public ByteBuffer readMappedFileCopyToRegularBuffer() throws IOException {
        var readBuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, fSize);
        normalByteBuffer.rewind();
        return normalByteBuffer.put(readBuf);
    }

    @Benchmark
    public ByteBuffer readMappedFileCopyDirect() throws IOException {
        var readBuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, fSize);
        directByteBuffer.rewind();
        return directByteBuffer.put(readBuf);
    }

    @Benchmark
    public MappedByteBuffer readMappedFileLoad() throws IOException {
        var readBuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, fSize);
        return readBuf.load();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ReadWritePerf.class.getSimpleName())
                .forks(0)
                .build();

        new Runner(opt).run();
    }
}
