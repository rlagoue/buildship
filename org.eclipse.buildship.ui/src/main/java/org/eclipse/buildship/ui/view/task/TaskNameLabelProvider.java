/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.view.task;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImageWithState;
import org.eclipse.buildship.ui.PluginImages;

/**
 * Styled label provider for the task name column in the TaskView.
 */
public final class TaskNameLabelProvider extends LabelProvider implements IStyledLabelProvider {

    private final ILabelProvider workbenchLabelProvider;

    public TaskNameLabelProvider() {
        this.workbenchLabelProvider = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
    }

    @Override
    public String getText(Object element) {
        return element == null ? "" : getStyledText(element).getString();
    }

    @Override
    public StyledString getStyledText(Object element) {
        if (element instanceof ProjectTaskNode) {
            return getProjectTaskText((ProjectTaskNode) element);
        } else if (element instanceof TaskSelectorNode) {
            return getTaskSelectorText((TaskSelectorNode) element);
        } else if (element instanceof ProjectNode) {
            return getProjectText((ProjectNode) element);
        } else if (element instanceof TaskGroupNode) {
            return getGroupText((TaskGroupNode) element);
        } else {
            throw new IllegalStateException(String.format("Unknown element type of element %s.", element));
        }
    }


    @Override
    public Image getImage(Object element) {
        if (element instanceof ProjectTaskNode) {
            return getProjectTaskImage((ProjectTaskNode) element);
        } else if (element instanceof TaskSelectorNode) {
            return getTaskSelectorImage((TaskSelectorNode) element);
        } else if (element instanceof ProjectNode) {
            return getProjectImage((ProjectNode) element);
        } else if (element instanceof TaskGroupNode) {
            return getGroupImage((TaskGroupNode) element);
        } else {
            throw new IllegalStateException(String.format("Unknown element type of element %s.", element));
        }
    }

    private StyledString getTaskSelectorText(TaskSelectorNode taskSelector) {
        return new StyledString(taskSelector.getTaskSelector().getName());
    }

    private StyledString getProjectTaskText(ProjectTaskNode projectTask) {
        return new StyledString(projectTask.getProjectTask().getName());
    }

    private StyledString getGroupText(TaskGroupNode group) {
        return new StyledString(group.getName());
    }

    private StyledString getProjectText(ProjectNode project) {
        String name;
        Optional<IProject> workspaceProject = project.getWorkspaceProject();
        if (workspaceProject.isPresent()) {
            name = workspaceProject.get().getName();
        } else {
            name = project.getEclipseProject().getName();
        }
        return new StyledString(name);
    }

    private Image getProjectImage(ProjectNode project) {
        Optional<IProject> workspaceProject = project.getWorkspaceProject();
        if (workspaceProject.isPresent()) {
            return this.workbenchLabelProvider.getImage(workspaceProject.get());
        } else {
            return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT_CLOSED);
        }
    }

    private Image getGroupImage(TaskGroupNode element) {
        return PluginImages.TASK_GROUP.withState(ImageState.ENABLED).getImage();
    }

    private Image getProjectTaskImage(ProjectTaskNode projectTask) {
        return getOverlayImageForProjectTask(projectTask);
    }

    private Image getTaskSelectorImage(TaskSelectorNode taskSelector) {
        return getOverlayImageForTaskSelector(taskSelector);
    }

    private Image getOverlayImageForProjectTask(ProjectTaskNode projectTask) {
        ImmutableList.Builder<PluginImageWithState> overlayImages = ImmutableList.builder();
        overlayImages.add(PluginImages.OVERLAY_PROJECT_TASK.withState(ImageState.ENABLED));
        if (!projectTask.isPublic()) {
            overlayImages.add(PluginImages.OVERLAY_PRIVATE_TASK.withState(ImageState.ENABLED));
        }
        return getOverlayImage(projectTask, overlayImages.build());
    }

    private Image getOverlayImageForTaskSelector(TaskSelectorNode taskSelector) {
        ImmutableList.Builder<PluginImageWithState> overlayImages = ImmutableList.builder();
        overlayImages.add(PluginImages.OVERLAY_TASK_SELECTOR.withState(ImageState.ENABLED));
        if (!taskSelector.isPublic()) {
            overlayImages.add(PluginImages.OVERLAY_PRIVATE_TASK.withState(ImageState.ENABLED));
        }
        return getOverlayImage(taskSelector, overlayImages.build());
    }

    private Image getOverlayImage(TaskNode taskNode, List<PluginImageWithState> overlayImages) {
        ImageState state = taskNode.getParentProjectNode().isIncludedProject() ? ImageState.DISABLED : ImageState.ENABLED;
        return PluginImages.TASK.withState(state).getOverlayImage(overlayImages);
    }

    @Override
    public void dispose() {
        this.workbenchLabelProvider.dispose();
        super.dispose();
    }

}
