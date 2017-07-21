import java.io.Serializable;
import java.util.HashMap;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.File;
import java.util.ArrayList;
import java.nio.file.Files;
import java.io.IOException;
import java.util.Scanner;
import java.nio.file.StandardCopyOption;

public class CommitTree implements Serializable {
    private HashMap<String, ArrayList<CommitNode>> messageToNodes = new 
        HashMap<String, ArrayList<CommitNode>>();
    private HashMap<Integer, CommitNode> idToNodes = new HashMap<Integer, CommitNode>();
    private HashMap<String, CommitNode> branchesToHeads = new HashMap<String, CommitNode>();
    private String currentBranch;
    private CommitNode currentHead;
    private int commitID;

    public CommitTree(Date date) {
        currentHead = new 
            CommitNode(0, "initial commit", date, null, new HashMap<String, String>());
        currentBranch = "master";
        branchesToHeads.put(currentBranch, currentHead);
        idToNodes.put(0, currentHead);
        ArrayList<CommitNode> newNodeList = new ArrayList<CommitNode>();
        newNodeList.add(currentHead);
        messageToNodes.put("initial commit", newNodeList);
    }

    public int incrementID() {
        commitID++;
        //System.out.println("incremented ID to " + commitID);
        return commitID;
    }

    public int getCommitID() {
        return commitID;
    }

    public void insertCommit(String message, Date date, HashMap<String, String> allFilePaths) {
        CommitNode commitNode = new CommitNode(commitID, message, date, currentHead, allFilePaths);
        currentHead = commitNode;
        branchesToHeads.put(currentBranch, currentHead);
        idToNodes.put(commitID, currentHead);
        if (messageToNodes.get(message) != null) {
            messageToNodes.get(message).add(currentHead);
        } else {
            ArrayList<CommitNode> newNodeList = new ArrayList<CommitNode>();
            newNodeList.add(currentHead);
            messageToNodes.put(message, newNodeList);
        }
    }

    public CommitNode getHeadNode() {
        return currentHead;
    }

    public String currentBranch() {
        return currentBranch;
    }

    public void printGlobalLog() {
        CommitNode temp;
        for (Integer idNumber : idToNodes.keySet()) {
            temp = idToNodes.get(idNumber);
            System.out.println("====");
            System.out.println("Commit " + temp.commitID + ".");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(dateFormat.format(temp.date));
            System.out.println(temp.commitMessage);
            System.out.println(""); 
        }
    }

    public void printLog() {
        CommitNode temp = currentHead;
        while (temp != null) {
            System.out.println("====");
            System.out.println("Commit " + temp.commitID + ".");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(dateFormat.format(temp.date));
            System.out.println(temp.commitMessage);
            System.out.println("");
            temp = temp.prev;
        }
    }

    public void find(String message) {
        ArrayList<CommitNode> nodesWithMessage = messageToNodes.get(message);
        for (CommitNode node : nodesWithMessage) {
            System.out.println(node.commitID);
        }
    }

    public String getPath(String filename) {
        return currentHead.filenamesToPaths.get(filename);
    }

    public String getFileFromCommitID(Integer id, String filename) {
        CommitNode nodeFromID = idToNodes.get(id);
        if (nodeFromID != null) {
            return nodeFromID.filenamesToPaths.get(filename);
        } else {
            return "No commit with that id exists.";
        }
    }

    public String getCommitPath(String filename) {
        return currentHead.filenamesToPaths.get(filename);
    }

    public HashMap<String, String> checkoutBranch(String branchName) {
        CommitNode branchHead = branchesToHeads.get(branchName);
        if (branchHead != null) {
            currentBranch = branchName;
            currentHead = branchHead;
            return currentHead.filenamesToPaths;
        } else {
            return null;
        }
    }

    public void status(StagedFiles stagedFiles, MarkedFiles markedFiles) {
        System.out.println("=== Branches ===");
        for (String branch : branchesToHeads.keySet()) {
            if (branch.equals(currentBranch)) {
                System.out.print("*");
            }
            System.out.println(branch);
        }
        System.out.println("");
        System.out.println("=== Staged Files ===");
        for (String stagedFile : stagedFiles.getFilenames()) {
            System.out.println(stagedFile);
        }
        System.out.println("");
        System.out.println("=== Files Marked for Removal ===");
        for (String markedFile : markedFiles.getFilenames()) {
            System.out.println(markedFile);
        }
        System.out.println("");
    }

    public HashMap<String, String> getFilenamesToPaths() {
        return currentHead.filenamesToPaths;
    }

    public boolean newBranch(String branchName) {
        if (branchesToHeads.containsKey(branchName)) {
            return false;
        } else {
            branchesToHeads.put(branchName, currentHead);
            return true;
        }
    }

    public boolean rmBranch(String branchName) {
        if (branchesToHeads.containsKey(branchName)) {
            return true;
        } else {
            branchesToHeads.remove(branchName);
            return false;
        }
    }

    public boolean mergeBranch(String givenBranch) {
        CommitNode givenBranchHead = branchesToHeads.get(givenBranch);
        if (givenBranchHead == null) {
            return false;
        } else {    
            CommitNode splitPoint = currentHead;
            HashMap<String, String> headFilesToPaths = currentHead.filenamesToPaths;
            CommitNode givenBranchPointer = givenBranchHead;
            HashMap<String, String> branchFilesToPaths = givenBranchHead.filenamesToPaths;
            if (splitPoint.commitID == givenBranchPointer.commitID) {
                return true;
            }
            int splitID = 0; outerloop:
            while (splitPoint != null) {
                while (givenBranchPointer != null) {
                    if (splitPoint.commitID == givenBranchPointer.commitID) {
                        splitID = splitPoint.commitID; break outerloop;
                    }
                    givenBranchPointer = givenBranchPointer.prev;
                }
                givenBranchPointer = givenBranchHead; splitPoint = splitPoint.prev;
            }
            File commitedFile; File oldFile;
            HashMap<String, String> splitFilesToPaths = idToNodes.get(splitID).filenamesToPaths;
            String headFilePath; String branchFilePath; String splitFilePath;
            for (String branchFile : branchFilesToPaths.keySet()) {
                headFilePath = headFilesToPaths.get(branchFile);
                branchFilePath = branchFilesToPaths.get(branchFile);
                splitFilePath = splitFilesToPaths.get(branchFile);
                oldFile = new File(branchFile);
                commitedFile = new File(branchFilePath);
                if (branchFilePath != splitFilePath && headFilePath == splitFilePath 
                    && splitFilePath != null) {
                    try {
                        Files.copy(commitedFile.toPath(), oldFile.toPath(), 
                            StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                } else if (branchFilePath != splitFilePath 
                    && splitFilePath != null && headFilePath == null) {
                    try {
                        Files.copy(commitedFile.toPath(), oldFile.toPath(), 
                            StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                } else if (splitFilePath == null && headFilePath == null) {
                    try {
                        Files.copy(commitedFile.toPath(), oldFile.toPath(), 
                            StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                } else if (splitFilePath != null && branchFilePath != splitFilePath 
                    && headFilePath != splitFilePath) {
                    oldFile = new File(branchFile + ".conflicted");
                    commitedFile = new File(branchFilePath);
                    try {
                        Files.copy(commitedFile.toPath(), oldFile.toPath(), 
                            StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                } else if (headFilePath != null && headFilePath != splitFilePath 
                    && branchFilePath != splitFilePath) {
                    oldFile = new File(branchFile + ".conflicted");
                    commitedFile = new File(branchFilePath);
                    try {
                        Files.copy(commitedFile.toPath(), oldFile.toPath(), 
                            StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println(e);
                    }           
                }               
            }
            return true;
        }
    }

    public boolean rebase(String givenBranch) {
        CommitNode givenBranchHead = branchesToHeads.get(givenBranch);
        if (givenBranchHead == null) {
            return false;
        } else {
            if (givenBranchHead.commitID == currentHead.commitID) {
                return true;
            }   
            CommitNode splitPoint = currentHead;
            HashMap<String, String> headFilesToPaths = currentHead.filenamesToPaths;
            CommitNode givenBranchPointer = givenBranchHead;
            HashMap<String, String> branchFilesToPaths = givenBranchHead.filenamesToPaths;
            ArrayList<CommitNode> nodesFromGivenToSplit = new ArrayList<CommitNode>();
            CommitNode splitPointerCopy;
            int splitID = 0;
            String message = splitPoint.commitMessage;
            outerloop:
            while (splitPoint != null) {
                message = splitPoint.commitMessage;
                splitPointerCopy = new CommitNode(commitID, message, 
                    new Date(), currentHead, splitPoint.filenamesToPaths);
                nodesFromGivenToSplit.add(splitPointerCopy);
                while (givenBranchPointer != null) {
                    if (splitPoint.commitID == givenBranchPointer.commitID) {
                        splitID = splitPoint.commitID;
                        break outerloop;
                    }
                    givenBranchPointer = givenBranchPointer.prev;
                }
                givenBranchPointer = givenBranchHead;
                splitPoint = splitPoint.prev;
            }
            //System.out.println("splitID: " + splitID);
            if (currentHead.commitID == splitID) { //check if this is right
                currentHead = givenBranchHead;
                branchesToHeads.put(currentBranch, givenBranchHead);
                return true;
            }
            if (givenBranchHead.commitID == splitID) { //check if this is right
                System.out.println("Already up-to-date.");
                return true;
            }
            CommitNode n;
            //System.out.println("new nodes list size: " + nodesFromGivenToSplit.size());
            for (int i = nodesFromGivenToSplit.size() - 2; i >= 0; i--) {
                commitID++;
                n = nodesFromGivenToSplit.get(i);
                n.commitID = commitID;
                //System.out.println("CommidID in Loop: " + commitID);
                if (i == nodesFromGivenToSplit.size() - 2) {
                    n.prev = givenBranchHead;
                } else {
                    n.prev = nodesFromGivenToSplit.get(i + 1);
                }
                idToNodes.put(commitID, n);
                messageToNodes.get(message).add(n);
                if (i == 0) {
                    currentHead = nodesFromGivenToSplit.get(i);
                    branchesToHeads.put(currentBranch, currentHead);
                }
            }
            rebasepart2(splitID, nodesFromGivenToSplit, givenBranchHead);
            return true;
        }
    }

    private void rebasepart2(int splitID, 
            ArrayList<CommitNode> nodesFromGivenToSplit, CommitNode givenBranchHead) {
        File commitedFile;
        File oldFile;
        HashMap<String, String> splitFilesToPaths = idToNodes.get(splitID).filenamesToPaths;
        HashMap<String, String> branchFilesToPaths = givenBranchHead.filenamesToPaths;
        HashMap<String, String> headFilesToPaths = currentHead.filenamesToPaths;
        String headFilePath;
        String branchFilePath;
        String splitFilePath;
        CommitNode n;
        headFilesToPaths = currentHead.filenamesToPaths;
        for (String branchFile : branchFilesToPaths.keySet()) {
            headFilePath = headFilesToPaths.get(branchFile);
            branchFilePath = branchFilesToPaths.get(branchFile);
            splitFilePath = splitFilesToPaths.get(branchFile);
            if (branchFilePath != splitFilePath 
                && splitFilePath == headFilePath && splitFilePath != null) {
                oldFile = new File(branchFile);
                commitedFile = new File(branchFilePath);
                for (int i = nodesFromGivenToSplit.size() - 2; i >= 0; i--) {
                    n = nodesFromGivenToSplit.get(i);
                    n.filenamesToPaths.put(branchFile, branchFilePath);
                }
                try {
                    Files.copy(commitedFile.toPath(), 
                        oldFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println(e);
                }   
            } else if (branchFilePath != splitFilePath 
                && splitFilePath == null && headFilePath == null) {
                oldFile = new File(branchFile);
                commitedFile = new File(branchFilePath);
                for (int i = nodesFromGivenToSplit.size() - 2; i >= 0; i--) {
                    n = nodesFromGivenToSplit.get(i);
                    n.filenamesToPaths.put(branchFile, branchFilePath);
                }   
                try {
                    Files.copy(commitedFile.toPath(), oldFile.toPath(), 
                        StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println(e);
                }
            }               
        }
        for (String headFile:headFilesToPaths.keySet()) {
            oldFile = new File(headFile);
            commitedFile = new File(headFilesToPaths.get(headFile));
            try {
                Files.copy(commitedFile.toPath(), oldFile.toPath(), 
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    public boolean irebase(String givenBranch) {
        CommitNode givenBranchHead = branchesToHeads.get(givenBranch);
        CommitNode n = currentHead;
        if (givenBranchHead == null) {
            return false;
        } else {
            if (givenBranchHead.commitID == currentHead.commitID) {
                return true;
            }   
            CommitNode splitPoint = currentHead;
            HashMap<String, String> headFilesToPaths = currentHead.filenamesToPaths;
            CommitNode givenBranchPointer = givenBranchHead;
            HashMap<String, String> branchFilesToPaths = givenBranchHead.filenamesToPaths;
            ArrayList<CommitNode> nodesFromGivenToSplit = new ArrayList<CommitNode>();
            CommitNode splitPointerCopy;
            int splitID = 0; String message = splitPoint.commitMessage; outerloop:
            while (splitPoint != null) {
                message = splitPoint.commitMessage;
                splitPointerCopy = new CommitNode(commitID, message, 
                    new Date(), currentHead, splitPoint.filenamesToPaths);
                nodesFromGivenToSplit.add(splitPointerCopy);
                while (givenBranchPointer != null) {
                    if (splitPoint.commitID == givenBranchPointer.commitID) {
                        splitID = splitPoint.commitID; break outerloop;
                    }
                    givenBranchPointer = givenBranchPointer.prev;
                }
                givenBranchPointer = givenBranchHead; splitPoint = splitPoint.prev;
            }
            if (currentHead.commitID == splitID) { //check if this is right
                currentHead = givenBranchHead;
                branchesToHeads.put(currentBranch, givenBranchHead); return true;
            }
            if (givenBranchHead.commitID == splitID) { //check if this is right
                System.out.println("Already up-to-date."); return true;
            }
            int prevCounter = 0; CommitNode temp;
            for (int i = nodesFromGivenToSplit.size() - 2; i >= 0; i--) {
                temp = nodesFromGivenToSplit.get(i);
                Scanner userInputScanner = new Scanner(System.in);
                String permission = ""; rebasePrint(temp);
                while (!permission.equals("s") 
                    && !permission.equals("c") && !permission.equals("m")) {
                    System.out.println("Would you like to (c)ontinue, "
                        + "(s)kip this commit, or change this commit's (m)essage?");
                    if (permission.equals("s") && i == 0 || permission.equals("s") 
                        && i == nodesFromGivenToSplit.size() - 2) {
                        while (!permission.equals("c") && !permission.equals("m")) {
                            System.out.println("Would you like to (c)ontinue, "
                                + "(s)kip this commit, or change this commit's (m)essage?");
                            permission = userInputScanner.nextLine();
                        }
                    }
                    permission = userInputScanner.nextLine();
                }
                if (permission.equals("s")) {
                    n = nodesFromGivenToSplit.get(i);
                    n.prev = nodesFromGivenToSplit.get(i); prevCounter++; continue;
                } else if (permission.equals("c")) {
                    commitID++; n = nodesFromGivenToSplit.get(i); n.commitID = commitID;
                    if (i == nodesFromGivenToSplit.size() - 2) {
                        n.prev = givenBranchHead;
                    } else {
                        n.prev = nodesFromGivenToSplit.get(i + prevCounter);
                    }
                    idToNodes.put(commitID, n); messageToNodes.get(message).add(n);
                    if (i == 0) {
                        currentHead = nodesFromGivenToSplit.get(i);
                        branchesToHeads.put(currentBranch, currentHead);
                    }
                    prevCounter = 0;
                } else if (permission.equals("m")) { 
                    prevCounter = lastPartOfirebase(n, 
                        message, i, nodesFromGivenToSplit, givenBranchHead);
                }
            }
            irebasepart2(splitID, nodesFromGivenToSplit, givenBranchHead);
            return true;
        }
    }

    private int lastPartOfirebase(CommitNode n, String message, int i,
        ArrayList<CommitNode> nodesFromGivenToSplit, CommitNode givenBranchHead) {
        System.out.println("Please enter a new message for this commit.");
        Scanner userInputScanner = new Scanner(System.in);
        String commitMess = userInputScanner.nextLine();
        commitID++; n = nodesFromGivenToSplit.get(i); n.commitID = commitID;
        n.commitMessage = commitMess;
        if (i == nodesFromGivenToSplit.size() - 2) {
            n.prev = givenBranchHead;
        } else {
            n.prev = nodesFromGivenToSplit.get(i + 1);
        }
        idToNodes.put(commitID, n);
        messageToNodes.get(message).add(n);
        if (i == 0) {
            currentHead = nodesFromGivenToSplit.get(i);
            branchesToHeads.put(currentBranch, currentHead);
        }
        return 0;
    }

    private void rebasePrint(CommitNode temp) {
        System.out.println("Currently replaying:");
        System.out.println("===="); System.out.println("Commit " + temp.commitID + ".");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(dateFormat.format(temp.date));
        System.out.println(temp.commitMessage);
        System.out.println("");
    }

    private void irebasepart2(int splitID, 
            ArrayList<CommitNode> nodesFromGivenToSplit, CommitNode givenBranchHead) {
        CommitNode n; File commitedFile; File oldFile;
        HashMap<String, String> splitFilesToPaths = idToNodes.get(splitID).filenamesToPaths;
        HashMap<String, String> branchFilesToPaths = givenBranchHead.filenamesToPaths;
        HashMap<String, String> headFilesToPaths = currentHead.filenamesToPaths;
        String headFilePath; String branchFilePath; String splitFilePath;
        headFilesToPaths = currentHead.filenamesToPaths;
        for (String branchFile : branchFilesToPaths.keySet()) {
            headFilePath = headFilesToPaths.get(branchFile);
            branchFilePath = branchFilesToPaths.get(branchFile);
            splitFilePath = splitFilesToPaths.get(branchFile);
            if (branchFilePath != splitFilePath && splitFilePath 
                == headFilePath && splitFilePath != null) {
                for (int i = nodesFromGivenToSplit.size() - 2; i >= 0; i--) {
                    n = nodesFromGivenToSplit.get(i);
                    n.filenamesToPaths.put(branchFile, branchFilePath);
                    oldFile = new File(branchFile); commitedFile = new File(branchFilePath);
                    try {
                        Files.copy(commitedFile.toPath(), oldFile.toPath(), 
                            StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                }   
            } else if (branchFilePath != splitFilePath && splitFilePath == null 
                && headFilePath == null) {
                for (int i = nodesFromGivenToSplit.size() - 2; i >= 0; i--) {
                    n = nodesFromGivenToSplit.get(i);
                    n.filenamesToPaths.put(branchFile, branchFilePath);
                    oldFile = new File(branchFile); commitedFile = new File(branchFilePath);
                    try {
                        Files.copy(commitedFile.toPath(), oldFile.toPath(), 
                            StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                }   
            }               
        }
        for (String headFile:headFilesToPaths.keySet()) {
            oldFile = new File(headFile);
            commitedFile = new File(headFilesToPaths.get(headFile));
            try {
                Files.copy(commitedFile.toPath(), oldFile.toPath(), 
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }
    public HashMap<String, String> reset(String commitIDNum) {
        CommitNode resetNode = idToNodes.get(Integer.parseInt(commitIDNum));
        if (resetNode == null) {
            return null;
        } else {
            currentHead = resetNode;
            branchesToHeads.put(currentBranch, currentHead);
            return currentHead.filenamesToPaths;
        }
    }

    private class CommitNode implements Serializable {
        private CommitNode(int id, String message, Date date, 
                CommitNode prev, HashMap<String, String> allFilePaths) {
            commitID = id;
            commitMessage = message;
            this.date = date;
            this.prev = prev;
            filenamesToPaths = allFilePaths;
        }

        private int commitID;
        private CommitNode prev;
        private HashMap<String, String> filenamesToPaths;
        private Date date;
        private String commitMessage;

    }
}
