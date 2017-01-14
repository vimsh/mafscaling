package com.vgi.mafscaling;

import java.io.File;
import javax.swing.filechooser.FileSystemView;

public class RestrictedFileSystemView  extends FileSystemView {
    private final File rootDirectory;
    RestrictedFileSystemView(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }
    @Override
    public File createNewFolder(File dir) {
        return null;
    }
    @Override
    public File getDefaultDirectory() {
        return rootDirectory;
    }
    @Override
    public File getHomeDirectory() {
        return rootDirectory;
    }
    @Override
    public File getParentDirectory(File dir) {
        return rootDirectory;
    }
    @Override
    public File[] getRoots() {
        return new File[] {rootDirectory};
    }
    @Override
    public boolean isRoot(File file) {
        if (file.equals(rootDirectory))
            return true;
        return false;
    }
    @Override
    public boolean isFileSystemRoot(File file) {
        return isRoot(file);
    }
}
