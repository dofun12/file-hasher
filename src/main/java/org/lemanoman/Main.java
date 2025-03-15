package org.lemanoman;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class Main {
    public enum HashType {
        MD5, SHA256
    }



    public static List<File> fastScan(ScanRequest... request) {
        List<File> dups = new ArrayList<>();
        Map<String, List<HashResult>> results = new HashMap<>();
        //File largeOne = new File("D:\\youtube_dl", "686eaf2de430e2645ecda52def477810540b1caa07c84081d230193bbb4f40ab.mp4" );
        long files = 0;
        long totalTimeTaken = 0;
        double totalSizeScanned = 0;
        List<File> allFiles = new ArrayList<>();
        for(ScanRequest req:request){
            if(req.recursive()){
                allFiles.addAll(FileUtils.listFiles(req.file()));
            }
        }
        for (File file : allFiles) {
            if(file.isDirectory()){
                continue;
            }
            files++;
            var result = HashUtil.getPartialHash(file, HashType.MD5, 4, 1024);
            totalTimeTaken+=result.timeTaken();
            totalSizeScanned+=result.size();
            System.out.println(file.getName()+": " +result);
            List<HashResult> hashResults = new ArrayList<>();
            if(results.containsKey(result.hash())){
                hashResults = results.get(result.hash());
                hashResults.add(result);
            }else {
                hashResults.add(result);
            }
            results.put(result.hash(), hashResults);
        }
        long uniqueHash = results.keySet().size();
        double sizeCanBeSaved = 0;
        for (Map.Entry<String, List<HashResult>> entry : results.entrySet()) {
            System.out.println("Hash: " + entry.getKey());
            int size = entry.getValue().size();
            if(size<=1){
                continue;
            }
            boolean isFirst = true;
            for (HashResult hashResult : entry.getValue()) {
                System.out.print("\t "+hashResult.file() + " - " + SizeUtil.humanReadableByteCount(hashResult.file().length()));
                if(isFirst){
                    isFirst = false;
                    System.out.print(" (Original) \n");
                    continue;
                }
                dups.add(hashResult.file());
                sizeCanBeSaved+=hashResult.file().length();
                System.out.print(" (Duplicado)\n");
            }
        }
        System.out.println("Files: " + files);
        System.out.println("Size Can be Saved: " + SizeUtil.humanReadableByteCount(sizeCanBeSaved));
        System.out.println("Unique Hash: " + uniqueHash);
        System.out.println("Total Time Taken: " + totalTimeTaken+"ms");
        System.out.println("Total Size Scanned: " + totalSizeScanned+" bytes");
        return dups;
    }

    public static void main(String[] args) throws FileNotFoundException {

        System.out.println("Hello world!");
        // fastScan(new File("D:\\hash-tests"));
        // fastScan(new File("D:\\youtube_dl"));
        // fastScan(new File("D:\\temp"));
        fastScan(
                new ScanRequest( new File("E:\\Downloads"), false),
                new ScanRequest( new File("E:\\WinFiles\\mp4"), true),
                new ScanRequest( new File("D:\\youtube_dl"), true),
                new ScanRequest( new File("D:\\data\\mp4"), true),
                new ScanRequest( new File("D:\\Nova pasta"), true),
                new ScanRequest( new File("E:\\hitomi-all"), true),
                new ScanRequest( new File("E:\\mnt"), true),
                new ScanRequest( new File("D:\\Hitomi\\hitomi_downloader_GUI"), true)

        );


    }




    public static String hash256Partial(File largeOne) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(largeOne, "rw");
        long size =  largeOne.length();
        final long cursorSize = 1024*1024;
        byte[] byteCursorStart = new byte[(int)cursorSize];
        byte[] byteCursorEnd = new byte[(int)cursorSize];
        System.out.println("Size: " + size);
        System.out.println("Chunks of 1kb each: " + size/1024);
        System.out.println("Chunks of 1mb each: " + size/1024/1024);
        final Long end = size-1;
        final long endPart = end-cursorSize;
        try {
            raf.read(byteCursorStart, 0, (int) cursorSize);
            raf.seek(endPart);
            raf.read(byteCursorEnd);
            System.out.println(HashUtil.getMD5Hash(byteCursorStart));
            System.out.println(HashUtil.getMD5Hash(byteCursorEnd));
            System.out.println(HashUtil.getSHA256Hash(byteCursorStart));
            System.out.println(HashUtil.getSHA256Hash(byteCursorEnd));
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}