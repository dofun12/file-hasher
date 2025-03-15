package org.lemanoman;

import java.io.File;

public record ScanRequest(File file, boolean recursive) {
}
