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

    public static HashResult splitFile(File file, HashType hashType, final long fragments, final long maxPartSize) {
        List<byte[]> parts = new ArrayList<>((int) fragments);
        long size = file.length();

        long portion = size/fragments;
        System.out.println("Portion: " + portion);
        long part = portion;
        if(portion>=maxPartSize){
            System.out.println("Portion is greater than 1mb");
            portion = maxPartSize;
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "r");){

            for(int i=0;i<fragments;i++){
                final byte[] partBytes = readPartial(raf,i*part,portion);
                parts.add(partBytes);
            }
            raf.close();
            System.out.println("Parts: " + parts.size());
            long totalPartSize = 0;
            for(byte[] partBytes:parts){
                totalPartSize+=partBytes.length;
            }
            byte[] finalBytes = new byte[(int)totalPartSize];
            for (int i = 0; i < parts.size(); i++) {
                byte[] partBytes = parts.get(i);
                //HashUtil.getSHA256Hash(partBytes);
                System.arraycopy(partBytes, 0, finalBytes, i * partBytes.length, partBytes.length);
            }
            long start = System.currentTimeMillis();
            String hash = "";
            if(hashType==HashType.MD5){
                hash = HashUtil.getMD5Hash(finalBytes);
            } else {
                hash = HashUtil.getSHA256Hash(finalBytes);
            }
            //final String md5 = HashUtil.getMD5Hash(finalBytes);
            return new HashResult(file, hash, hashType.toString(), finalBytes.length, System.currentTimeMillis()-start);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public static List<File> fastScan(File baseDir){
        List<File> dups = new ArrayList<>();
        Map<String, List<HashResult>> results = new HashMap<>();
        //File largeOne = new File("D:\\youtube_dl", "686eaf2de430e2645ecda52def477810540b1caa07c84081d230193bbb4f40ab.mp4" );
        long files = 0;
        long totalTimeTaken = 0;
        double totalSizeScanned = 0;
        for (File file : baseDir.listFiles()) {
            if(file.isDirectory()){
                continue;
            }
            files++;
            var result = splitFile(file, HashType.MD5, 4, 1024);
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
                if(isFirst){
                    isFirst = false;
                    continue;
                }
                dups.add(hashResult.file());
                sizeCanBeSaved+=hashResult.file().length();
                System.out.println("\t "+hashResult.file() + " - " + SizeUtil.humanReadableByteCount(hashResult.file().length()));
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
        fastScan(new File("D:\\Downloads"));

    }


    public static byte[] readPartial(RandomAccessFile raf, long start, long cursorSize) throws IOException {
        byte[] byteCursor = new byte[(int) cursorSize];
        raf.seek(start);
        raf.read(byteCursor);
        return byteCursor;
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