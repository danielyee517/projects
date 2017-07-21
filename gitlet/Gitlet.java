import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;

/**
 * Class that provides JUnit tests for Gitlet, as well as a couple of utility
 * methods.
 * 
 * @author Daniel Yee
 * 
 *         Some code adapted from Mkyong.com:
 * 
 *         http://www.mkyong.com/java/how-to-convert-file-into-an-array-of-bytes/
 * 
 */

public class Gitlet {
    private static StagedFiles stagedFiles = new StagedFiles();
    private static MarkedFiles markedRemovalFiles = new MarkedFiles();
    private static CommitTree commitTree;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        switch (args[0]) {
            case "add":
                if (enoughArgs(args.length) < 2) {
                    return;
                }
                add(args[1]);
                return;
            case "init":
                initialize();
                return;  
            case "commit": 
                if (args.length < 2) {
                    System.out.println("Please enter a commit message.");
                    return;
                }
                commit(args[1]);
                return;
            case "log": 
                log();
                return;
            case "global-log": 
                globalLog();
                return;
            case "status": 
                status();
                return;
            case "find": 
                find(args);
                return;
            case "rm":
                remove(args);
                return;
            case "branch":
                if (enoughArgs(args.length) < 2) {
                    return;
                }
                branch(args[1]);
                return;
            case "rm-branch":
                if (enoughArgs(args.length) < 2) {
                    return;
                }
                rmbranch(args[1]);
                return;
            case "merge":
                if (enoughArgs(args.length) < 2) {
                    return;
                }
                merge(args[1]);
                return;
            case "rebase":
                if (enoughArgs(args.length) < 2) {
                    return;
                }
                rebase(args[1]);
                return;
            case "i-rebase":
                irebase(args);
                return;
            case "reset":
                reset(args);
                return;
            case "checkout":
                if (args.length >= 3) {
                    checkout(args[1], args[2]);
                } else if (args.length > 1) {
                    checkout(args[1]);
                }
                return;
            default:
                System.out.println("Unrecognized command.");  
                return;
        }
    }

    private static int enoughArgs(int argsLength) {
        if (argsLength < 2) {
            System.out.println("Did not enter enough arguments.");
        }
        return argsLength;
    }

    private static void commit(String message) {
        stagedFiles = (StagedFiles) deserialize(".gitlet/stagedFiles.ser");
        markedRemovalFiles = (MarkedFiles) deserialize(".gitlet/markedFiles.ser");
        commitTree = (CommitTree) deserialize(".gitlet/commitTree.ser");
        if (stagedFiles.size() == 0  && markedRemovalFiles.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        int commitID = commitTree.incrementID();
        new File(".gitlet/commits/" + commitID).mkdir();
        HashMap<String, String> newFilenames = new 
            HashMap<String, String>(commitTree.getFilenamesToPaths());
        File oldFile;
        File newFile;
        String directoriesString;
        //System.out.println("number of files staged: " + stagedFiles.getFilenames().size());
        for (String str : stagedFiles.getFilenames()) {
            if (str.contains("/")) {
                directoriesString = str.substring(0, str.lastIndexOf('/'));
                new File(".gitlet/commits/" + commitID + "/" + directoriesString).mkdirs();
            }
            //System.out.println(str);
            oldFile = new File(str);
            newFile = new File(".gitlet/commits/" + commitID + "/" + str);
            try {
                Files.copy(oldFile.toPath(), newFile.toPath());
            } catch (IOException e) {
                System.err.println(e);
            }
            newFilenames.put(str, ".gitlet/commits/" 
                + commitID + "/" + str); //make sure to check if you can remove something in remove
        }
        for (String str : markedRemovalFiles.getFilenames()) {
            newFilenames.remove(str); //make sure to check if you can remove something in remove
        }
        commitTree.insertCommit(message, new Date(), newFilenames);
        stagedFiles.clear();
        markedRemovalFiles.clear();
        try {
            serialize(markedRemovalFiles, ".gitlet/markedFiles.ser");
        } catch (IOException e) {
            System.err.println(e);
        }
        try {
            serialize(stagedFiles, ".gitlet/stagedFiles.ser");
        } catch (IOException e) {
            System.err.println(e);
        }
        try {
            serialize(commitTree, ".gitlet/commitTree.ser");
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private static void initialize() {
        File gitlet = new File(".gitlet"); 
        if (gitlet.exists() && gitlet.isDirectory()) { 
            System.out.println("A gitlet version control "
                + "system already exists in the current directory.");
        } else {
            gitlet.mkdir();
            new File(".gitlet/commits/0").mkdirs();
            try {
                serialize(markedRemovalFiles, ".gitlet/markedFiles.ser");
            } catch (IOException e) {
                System.err.println(e);
            }
            try {
                serialize(stagedFiles, ".gitlet/stagedFiles.ser");
            } catch (IOException e) {
                System.err.println(e);
            }
            commitTree = new CommitTree(new Date());
            try {
                serialize(commitTree, ".gitlet/commitTree.ser");
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    private static void remove(String[] args) {
        if (args.length < 2) {
            System.out.println("Did not enter enough arguments.");
            return;
        }
        String filename = args[1];
        File f = new File(filename);
        stagedFiles = (StagedFiles) deserialize(".gitlet/stagedFiles.ser");
        markedRemovalFiles = (MarkedFiles) deserialize(".gitlet/markedFiles.ser");
        commitTree = (CommitTree) deserialize(".gitlet/commitTree.ser");
        if (markedRemovalFiles.contains(filename)) {
            //System.out.println("returned because already marked");
            return;
        }
        //System.out.println("filename = " + filename);
        String path = commitTree.getPath(filename); //get file from headNode
        //System.out.println("path is " + path);
        if (path == null && !stagedFiles.contains(filename)) {
            System.out.println("No reason to remove the file.");
        } else {
            if (stagedFiles.contains(filename)) {
                stagedFiles.remove(filename);
            } else { 
                markedRemovalFiles.add(filename);
            }
        }
        try {
            serialize(markedRemovalFiles, ".gitlet/markedFiles.ser");
            serialize(stagedFiles, ".gitlet/stagedFiles.ser");
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private static void add(String filename) {
        File f = new File(filename);
        //System.out.println("add1");
        if (f.exists() && !f.isDirectory()) {
            //System.out.println("add2");
            stagedFiles = (StagedFiles) deserialize(".gitlet/stagedFiles.ser");
            markedRemovalFiles = (MarkedFiles) deserialize(".gitlet/markedFiles.ser");
            commitTree = (CommitTree) deserialize(".gitlet/commitTree.ser");
            if (markedRemovalFiles.contains(filename)) {
                markedRemovalFiles.remove(filename);
            } else {
                if (stagedFiles.contains(filename)) {
                    //System.out.println("Returned because already staged");
                    return;
                }
                FileInputStream fileInputStream = null;  
                byte[] bFile = new byte[(int) f.length()];
                try {
                    //convert file into array of bytes
                    fileInputStream = new FileInputStream(f);
                    fileInputStream.read(bFile);
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //System.out.println("filename = " + filename);
                String path = commitTree.getPath(filename); //get file from headNode
                //System.out.println("path is " + path);
                if (path == null) {
                    //System.out.println("path = null 2");
                    stagedFiles.add(filename);
                } else {
                    f = new File(path);
                    byte[] bFile2 = new byte[(int) f.length()];
                    try {
                        //convert file into array of bytes
                        fileInputStream = new FileInputStream(f);
                        fileInputStream.read(bFile2);
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!Arrays.equals(bFile, bFile2)) {
                        stagedFiles.add(filename);
                    } else {
                        System.out.println("File has not been modified since the last commit.");
                    }
                }
            }
            try {
                serialize(markedRemovalFiles, ".gitlet/markedFiles.ser");
                serialize(stagedFiles, ".gitlet/stagedFiles.ser");
            } catch (IOException e) {
                System.err.println(e);
            }
        } else {
            System.out.println("File does not exist.");
        }
    }

    private static void log() {
        commitTree = (CommitTree) deserialize(".gitlet/commitTree.ser");
        commitTree.printLog();
    }

    private static void status() {
        stagedFiles = (StagedFiles) deserialize(".gitlet/stagedFiles.ser");
        markedRemovalFiles = (MarkedFiles) deserialize(".gitlet/markedFiles.ser");
        commitTree = (CommitTree) deserialize(".gitlet/commitTree.ser");
        commitTree.status(stagedFiles, markedRemovalFiles);
    }

    private static void branch(String branchName) {
        commitTree = (CommitTree) deserialize(".gitlet/commitTree.ser");
        if (!commitTree.newBranch(branchName)) {
            System.out.println("A branch with that name already exists.");
        }
        try {
            serialize(commitTree, ".gitlet/commitTree.ser");
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private static void rmbranch(String branchName) {
        commitTree = (CommitTree) deserialize(".gitlet/commitTree.ser");
        if (branchName.equals(commitTree.currentBranch())) {
            System.out.println("Cannot remove the current branch.");
        }
        if (!commitTree.rmBranch(branchName)) {
            System.out.println("A branch without that name does not exist.");
        }
        try {
            serialize(commitTree, ".gitlet/commitTree.ser");
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private static void merge(String givenBranch) {
        if (!dangerousContinue()) {
            return;
        }
        commitTree = (CommitTree) deserialize(".gitlet/commitTree.ser");
        if (givenBranch.equals(commitTree.currentBranch())) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        if (!commitTree.mergeBranch(givenBranch)) {
            System.out.println("A branch with that name does not exist.");
        }
    }

    private static void rebase(String givenBranch) {
        if (!dangerousContinue()) {
            return;
        }
        commitTree = (CommitTree) deserialize(".gitlet/commitTree.ser");
        if (givenBranch.equals(commitTree.currentBranch())) {
            System.out.println("Cannot rebase a branch onto itself.");
            return;
        }
        if (!commitTree.rebase(givenBranch)) {
            System.out.println("A branch with that name does not exist.");
        }
        try {
            serialize(commitTree, ".gitlet/commitTree.ser");
        } catch (IOException e) {
            System.err.println(e);
        } 
    }

    private static void irebase(String[] args) {
        if (args.length < 2) {
            System.out.println("Did not enter enough arguments.");
            return;
        }
        String givenBranch = args[1];
        if (!dangerousContinue()) {
            return;
        }
        commitTree = (CommitTree) deserialize(".gitlet/commitTree.ser");
        if (givenBranch.equals(commitTree.currentBranch())) {
            System.out.println("Cannot rebase a branch onto itself.");
            return;
        }
        if (!commitTree.irebase(givenBranch)) {
            System.out.println("A branch with that name does not exist.");
        } else {
            try {
                serialize(commitTree, ".gitlet/commitTree.ser");
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    private static void reset(String[] args) {
        if (args.length < 2) {
            System.out.println("Did not enter enough arguments.");
            return;
        }
        String commitID = args[1];
        commitTree = (CommitTree) deserialize(".gitlet/commitTree.ser");
        HashMap<String, String> filesInHead = commitTree.reset(commitID);
        if (filesInHead == null) {
            System.out.println("No commit with that id exists.");
            return;
        } else {
            File oldFile;
            File commitedFile;
            for (String workingFile : filesInHead.keySet()) {
                //System.out.println("working File: " + workingFile);
                oldFile = new File(workingFile);
                commitedFile = new File(filesInHead.get(workingFile));
                try {
                    Files.copy(commitedFile.toPath(), oldFile.toPath(), 
                        StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
            try {
                serialize(commitTree, ".gitlet/commitTree.ser");
            } catch (IOException e) {
                System.err.println(e);
            }
            return;
        }
    }

    private static void find(String[] args) {
        if (args.length < 2) {
            System.out.println("Did not enter enough arguments.");
            return;
        }
        String message = args[1];
        commitTree = (CommitTree) deserialize(".gitlet/commitTree.ser");
        commitTree.find(message);
    }

    private static void globalLog() {
        commitTree = (CommitTree) deserialize(".gitlet/commitTree.ser");
        commitTree.printGlobalLog();
    }

    private static void checkout(String fileOrBranch) {
        if (!dangerousContinue()) {
            return;
        }
        commitTree = (CommitTree) deserialize(".gitlet/commitTree.ser");
        if (fileOrBranch.equals(commitTree.currentBranch())) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        HashMap<String, String> filesInHeadOfBranch = commitTree.checkoutBranch(fileOrBranch);
        if (filesInHeadOfBranch != null) {
            File oldFile;
            File commitedFile;
            for (String workingFile : filesInHeadOfBranch.keySet()) {
                //System.out.println("working File: " + workingFile);
                oldFile = new File(workingFile);
                commitedFile = new File(filesInHeadOfBranch.get(workingFile));
                try {
                    Files.copy(commitedFile.toPath(), oldFile.toPath(), 
                        StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
            try {
                serialize(commitTree, ".gitlet/commitTree.ser");
            } catch (IOException e) {
                System.err.println(e);
            }
            return;
        }

        String commitFilePath = commitTree.getCommitPath(fileOrBranch);
        if (commitFilePath != null) {
            File fileOld = new File(fileOrBranch);
            File fileCommited = new File(commitFilePath);
            try {
                Files.copy(fileCommited.toPath(), fileOld.toPath(), 
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println(e);
            }
        } else {
            System.out.println("File does not exist in the most recent commit, "
                + "or no such branch exists.");
        }
    }

    private static void checkout(String commitID, String filename) {
        if (!dangerousContinue()) {
            return;
        }
        commitTree = (CommitTree) deserialize(".gitlet/commitTree.ser");
        String filePath = commitTree.getFileFromCommitID(Integer.parseInt(commitID), filename);
        if (filePath.equals("No commit with that id exists.")) {
            System.out.println(filePath);
            return;
        } else if (filePath == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File oldFile = new File(filename);
        File commitedFile = new File(filePath);
        try {
            Files.copy(commitedFile.toPath(), oldFile.toPath(), 
                StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println(e);
        }       
    }

    private static boolean dangerousContinue() {
        Scanner userInputScanner = new Scanner(System.in);
        System.out.println("Warning: The command you entered may "
            + "alter the files in your working directory. "
            + "Uncommitted changes may be lost. Are you sure you want to continue? (yes/no)");
        String permission = userInputScanner.nextLine();
        if (permission.equals("yes")) {
            return true;
        } else {
            System.out.println("Did not type 'yes', so aborting.");
            return false;
        }
    }

    private static Object deserialize(String name) {
        Object myObject = null;
        File myFile = new File(name);
        if (myFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(myFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                myObject = objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading myObject.";
                System.out.println(msg);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading myObject.";
                System.out.println(msg);
            }
        }
        return myObject;
    }

    private static void serialize(Object o, String name) throws IOException {
        try {
            File serFile = new File(name);
            FileOutputStream fileOut = new FileOutputStream(serFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(o);
        } catch (IOException e) {
            String msg = "IOException while saving " + name;
            System.err.println(msg);
        }
    }
}
