# Java performance benchmarks
Measure various java things. 
- Ever wondered what is the fastest way to read a file in Java?
- How long does it take to read 1Gb into memory from disk? 
- How long does it take to allocate 1Gb array?
- How fast can you sum up all the numbers in an array?

All measurements are done with 
```
# CPU: 11th Gen Intel(R) Core(TM) i7-1185G7 @ 3.00GHz
# Disk: WESTERN DIGITAL NVME 1TB (sdbptpz-1t00)
# JMH version: 1.36
# VM version: JDK 17.0.1, OpenJDK 64-Bit Server VM, 17.0.1+12-39
```


## Array performance

- All measurements are done with the array size of 1Gb. So you can read the measurements as Gb/s 
- When doing int array it means size = 1GB / Integer.BYTES = 250,000 elements

**Key take-aways:**

1. Working with heap byte buffer or array is equally fast. 
2. Allocating off heap bytebuffers is 5x slower than on heap.

```text
Benchmark                            Mode  Cnt   Score   Error  Units
ArrayPerf.allocateArray             thrpt    5  30.449 ± 4.399  ops/s
ArrayPerf.allocateByteBuffer        thrpt    5  30.356 ± 1.115  ops/s
ArrayPerf.allocateDirectByteBuffer  thrpt    5   6.023 ± 2.279  ops/s
ArrayPerf.initalizeArray            thrpt    5  12.025 ± 1.898  ops/s
ArrayPerf.initalizeIntBuffer        thrpt    5  12.054 ± 1.110  ops/s
ArrayPerf.sumArray                  thrpt    5  10.229 ± 0.678  ops/s
ArrayPerf.sumByteBufAsIntBuffer     thrpt    5  10.249 ± 0.791  ops/s
ArrayPerf.sumIntBuffer              thrpt    5  10.320 ± 1.002  ops/s
```

## Read write performance 

- Each measurment is done using 1Gb file. So you can read measurements as Gb/s.
- NOTE: read performance is repeated many times, so these measurements represent time reading OS cached file!
- TODO: Add measurment for cold reads

Baseline - perfomance of dd

**DD Cold read: 1.7Gb/s**
```text
$ dd if=read_file9807666655674265121tmp of=/dev/null bs=4M
500+0 records in
500+0 records out
2097152000 bytes (2.1 GB, 2.0 GiB) copied, 1.24417 s, 1.7 GB/s
```

**DD Cached read: 6.0 Gb/s**
```text
$ dd if=read_file9807666655674265121tmp of=/dev/null bs=4M
500+0 records in
500+0 records out
2097152000 bytes (2.1 GB, 2.0 GiB) copied, 0.346916 s, 6.0 GB/s
```

**Key take-aways:**
1. `MemoryMapped.Load` is ~70% as fast as dd for a cached case

```text
Benchmark                                         Mode  Cnt  Score   Error  Units
ReadWritePerf.readAllBytes                       thrpt   10  2.564 ± 0.123  ops/s
ReadWritePerf.readMappedFileCopyDirect           thrpt   10  3.035 ± 0.143  ops/s
ReadWritePerf.readMappedFileCopyToRegularBuffer  thrpt   10  3.177 ± 0.190  ops/s
ReadWritePerf.readMappedFileLoad                 thrpt   10  4.295 ± 0.155  ops/s
ReadWritePerf.readNioStream                      thrpt   10  3.198 ± 0.069  ops/s
ReadWritePerf.writeFileNio                       thrpt   10  1.168 ± 0.026  ops/s
```