package org.lemanoman;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class Test {
    public static void main(String[] args) {
        TreeMap<String, String> map = new TreeMap<>();
        List<HashResult> results = new ArrayList<>();
        for (File file : FileUtils.listFiles(new File("E:\\mnt\\v2\\mp4"))) {
            System.out.println(file.getAbsolutePath());
            HashResult result = null;
            try {
                result = HashUtil.getFullSHA256(file);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            map.put(result.hash(), file.getName());
            System.out.println(result);
            results.add(result);
        }
        IndexerUtils.indexHash(results);

    }
}
