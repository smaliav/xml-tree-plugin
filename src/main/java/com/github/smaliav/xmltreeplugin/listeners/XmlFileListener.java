package com.github.smaliav.xmltreeplugin.listeners;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiManager;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.LinkedList;

public class XmlFileListener implements FileEditorManagerListener.Before {

    private final Project myProject;

    public XmlFileListener(Project project) {
        myProject = project;
    }

    @Override
    public void beforeFileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        String extension = file.getExtension();
        if (extension == null) return;

        if (extension.equals("xml")) {
            // TODO Remove Debug Output
            Messages.showInfoMessage("That is XMl!" , "XML Found!");

            // Obtain PSI tree
            XmlFile xmlFile = (XmlFile) PsiManager.getInstance(myProject).findFile(file);

            // Traverse PSI tree and construct Tree
            LinkedList<DefaultMutableTreeNode> nodeQueue = new LinkedList<>();

            xmlFile.accept(new XmlRecursiveElementVisitor(true) {
                @Override
                public void visitXmlTag(XmlTag tag) {
                    super.visitXmlTag(tag);

                    if (tag.getSubTags().length == 0) {
                        // Leaf Node
                        nodeQueue.add(new DefaultMutableTreeNode(tag.getText()));
                    } else {
                        // Branch Node
                        DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode(tag.getName());
                        int childrenCount = tag.getSubTags().length;
                        for (int i = 0; i < childrenCount; i++) {
                            int idx = nodeQueue.size() - childrenCount + i;
                            branchNode.add(nodeQueue.get(idx));
                            nodeQueue.remove(idx);
                        }
                        nodeQueue.add(branchNode);
                    }
                }
            });

            // Collect UI Tree
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("XML Tree");
            nodeQueue.forEach(root::add);
            nodeQueue.clear();

            // TODO Crashes when opens project with already opened XML file
            // TODO Wrap with scroll element
            // Register Tool Window
            Tree tree = new Tree(root);
            ToolWindowManager.getInstance(myProject).registerToolWindow(
                new RegisterToolWindowTask("XML", ToolWindowAnchor.BOTTOM, tree, false, true,
                    true, true, null, null, null)
            );
        }
    }

}
