package org.lemanoman;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexerUtils {
    public static void indexHash(List<HashResult> hashResults) {

        Map<String, List<HashResult>> indexedHashs = new HashMap<>();
        long files = hashResults.size();
        for (HashResult hashResult : hashResults) {

            List<HashResult> subHashes = new ArrayList<>();
            if (indexedHashs.containsKey(hashResult.hash())) {
                subHashes = indexedHashs.get(hashResult.hash());
            }
            subHashes.add(hashResult);
            indexedHashs.put(hashResult.hash(), subHashes);
        }
        long uniqueHash = indexedHashs.keySet().size();
        double sizeCanBeSaved = 0;
        for (Map.Entry<String, List<HashResult>> entry : indexedHashs.entrySet()) {
            System.out.println("Hash: " + entry.getKey());
            int size = entry.getValue().size();
            if (size <= 1) {
                continue;
            }
            boolean isFirst = true;
            for (HashResult hashResult : entry.getValue()) {
                System.out.print("\t " + hashResult.file() + " - " + SizeUtil.humanReadableByteCount(hashResult.file().length()));
                if (isFirst) {
                    isFirst = false;
                    System.out.print(" (Original) \n");
                    continue;
                }
                //dups.add(hashResult.file());
                sizeCanBeSaved += hashResult.file().length();
                System.out.print(" (Duplicado)\n");
            }
        }
        System.out.println("Files: " + files);
        System.out.println("Size Can be Saved: " + SizeUtil.humanReadableByteCount(sizeCanBeSaved));
        System.out.println("Unique Hash: " + uniqueHash);

    }
}
