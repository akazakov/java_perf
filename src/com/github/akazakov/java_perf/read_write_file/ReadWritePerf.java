package com.github.akazakov.java_perf.read_write_file;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
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
@Fork(1)
public class ReadWritePerf {

    int MB = 1024*1024;
    int size = 2000 * MB;
    Path dir = Paths.get("/tmp/java_perf");
    Path writePath;
    Path readPath;
    long fSize;

    byte[] out = new byte[size];
    byte[] in = new byte[size];
    ByteBuffer inBuf = ByteBuffer.wrap(in);
    ByteBuffer directBuf = ByteBuffer.allocateDirect(size);
    int total = 0;
    FileChannel fc;

    @Setup
    public void setup() {
        for (int i = 0; i < size; i++) {
            out[i] = (byte)i;
        }
        try {
            Files.createDirectories(dir);
            readPath = Files.createTempFile(dir, "read_file", "tmp");
            writePath = dir.resolve("test123");
            Files.write(readPath, out);
            fSize = Files.size(readPath);
            fc = FileChannel.open(readPath, StandardOpenOption.READ);
        } catch (IOException e) {
            System.out.println("Exception!" + e.getMessage());
        }
        System.out.println("Done construction! SIZE = " + size / MB + "Mb");
    }

    @TearDown
    public void tearDown() throws IOException, InterruptedException {
        fc.close();
        Thread.sleep(1000);
        System.out.println("Clean!");
    }


    @Benchmark
    public void writeFileNio() throws IOException {
        Files.write(writePath, out);
    }

    @Benchmark
    public void readAllBytes() throws IOException {
        total += Files.readAllBytes(readPath).length;
    }

    @Benchmark
    public void readNioStream() throws IOException {
        var is = Files.newInputStream(readPath);
        is.read(in);
        is.close();
    }

    @Benchmark
    public void readMappedFileCopy() throws IOException {
        var readBuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, fSize);
        inBuf.rewind();
        readBuf.rewind();
        inBuf.put(readBuf);
    }

    @Benchmark
    public void readMappedFileCopyDirect() throws IOException {
        var readBuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, fSize);
        directBuf.rewind();
        readBuf.rewind();
        directBuf.put(readBuf);
    }

    @Benchmark
    public void readMappedFileLoad() throws IOException {
        var readBuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, fSize);
        readBuf.load();
        readBuf.isLoaded();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ReadWritePerf.class.getSimpleName())
                .forks(0)
                .build();

        new Runner(opt).run();
    }

}
