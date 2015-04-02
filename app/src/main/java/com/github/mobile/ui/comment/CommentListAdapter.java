/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui.comment;


import android.app.Activity;
import android.content.res.Resources;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;

import com.github.kevinsawicki.wishlist.MultiTypeAdapter;
import com.github.mobile.R;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.HttpImageGetter;
import com.github.mobile.util.TimeUtils;
import com.github.mobile.util.TypefaceUtils;

import java.util.Collection;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IssueEvent;

/**
 * Adapter for a list of {@link Comment} objects
 */
public class CommentListAdapter extends MultiTypeAdapter {

    private final Resources resources;

    private final AvatarLoader avatars;

    private final HttpImageGetter imageGetter;

    /**
     * Callback listener to be invoked when user tries to edit a comment.
     */
     private final EditCommentListener editCommentListener;

    /**
     * Callback listener to be invoked when user tries to edit a comment.
     */
    private final DeleteCommentListener deleteCommentListener;

    /**
     * Create list adapter
     *
     * @param activity
     * @param elements
     * @param avatars
     * @param imageGetter
     */
    public CommentListAdapter(Activity activity, Comment[] elements, AvatarLoader avatars, HttpImageGetter imageGetter) {
        this(activity, elements, avatars, imageGetter, null, null);
    }

    /**
     * Create list adapter
     *
     * @param activity
     * @param avatars
     * @param imageGetter
     */
    public CommentListAdapter(Activity activity, AvatarLoader avatars, HttpImageGetter imageGetter) {
        this(activity, null, avatars, imageGetter);
    }

    /**
     * Create list adapter
     *
     * @param activity
     * @param avatars
     * @param imageGetter
     * @param editCommentListener
     * @param deleteCommentListener
     */
    public CommentListAdapter(Activity activity, Comment[] elements, AvatarLoader avatars, HttpImageGetter imageGetter,
            EditCommentListener editCommentListener, DeleteCommentListener deleteCommentListener) {
        super(activity.getLayoutInflater());

        this.resources = activity.getResources();
        this.avatars = avatars;
        this.imageGetter = imageGetter;
        this.editCommentListener = editCommentListener;
        this.deleteCommentListener = deleteCommentListener;
        setItems(elements);
    }

    @Override
    protected void update(int position, Object obj, int type) {
        if(type == 0)
            updateComment((Comment) obj);
        else
            updateEvent((IssueEvent) obj);
    }

    protected void updateEvent(final IssueEvent event) {
        String eventString = event.getEvent();

        String message;
        if (eventString.equals(IssueEvent.TYPE_ASSIGNED) || eventString.equals(IssueEvent.TYPE_UNASSIGNED)) {
            message = String.format("<b>%s</b> ", event.getAssignee().getLogin());
            avatars.bind(imageView(2), event.getAssignee());
        } else {
            message = String.format("<b>%s</b> ", event.getActor().getLogin());
            avatars.bind(imageView(2), event.getActor());
        }

        switch (eventString) {
        case IssueEvent.TYPE_ASSIGNED:
            message += String.format(resources.getString(R.string.issue_event_label_assigned));
            setText(0, TypefaceUtils.ICON_PERSON);
            textView(0).setTextColor(resources.getColor(R.color.issue_event_normal));
            break;
        case IssueEvent.TYPE_UNASSIGNED:
            message += String.format(resources.getString(R.string.issue_event_label_unassigned));
            setText(0, TypefaceUtils.ICON_PERSON);
            textView(0).setTextColor(resources.getColor(R.color.issue_event_normal));
            break;
        case IssueEvent.TYPE_LABELED:
            message += String.format(resources.getString(R.string.issue_event_label_added), "<b>" + event.getLabel().getName() + "</b>");
            setText(0, TypefaceUtils.ICON_TAG);
            textView(0).setTextColor(resources.getColor(R.color.issue_event_normal));
            break;
        case IssueEvent.TYPE_UNLABELED:
            message += String.format(resources.getString(R.string.issue_event_label_removed), "<b>" + event.getLabel().getName() + "</b>");
            setText(0, TypefaceUtils.ICON_TAG);
            textView(0).setTextColor(resources.getColor(R.color.issue_event_normal));
            break;
        case IssueEvent.TYPE_REFERENCED:
            message += String.format(resources.getString(R.string.issue_event_referenced), "<b>" + event.getCommitId().substring(0,7) + "</b>");
            setText(0, TypefaceUtils.ICON_BOOKMARK);
            textView(0).setTextColor(resources.getColor(R.color.issue_event_normal));
            break;
        case IssueEvent.TYPE_MILESTONED:
            message += String.format(resources.getString(R.string.issue_event_milestone_added), "<b>" + event.getMilestone().getTitle() + "</b>");
            setText(0, TypefaceUtils.ICON_MILESTONE);
            textView(0).setTextColor(resources.getColor(R.color.issue_event_normal));
            break;
        case IssueEvent.TYPE_DEMILESTONED:
            message += String.format(resources.getString(R.string.issue_event_milestone_removed), "<b>" + event.getMilestone().getTitle() + "</b>");
            setText(0, TypefaceUtils.ICON_MILESTONE);
            textView(0).setTextColor(resources.getColor(R.color.issue_event_normal));
            break;
        case IssueEvent.TYPE_CLOSED:
            message += resources.getString(R.string.issue_event_closed);
            setText(0, TypefaceUtils.ICON_CIRCLE_SLASH);
            textView(0).setTextColor(resources.getColor(R.color.issue_event_red));
            break;
        case IssueEvent.TYPE_REOPENED:
            message += resources.getString(R.string.issue_event_reopened);
            setText(0, TypefaceUtils.ICON_PRIMITIVE_DOT);
            textView(0).setTextColor(resources.getColor(R.color.issue_event_green));
            break;
        case IssueEvent.TYPE_RENAMED:
            message += resources.getString(R.string.issue_event_rename);
            setText(0, TypefaceUtils.ICON_PENCIL);
            textView(0).setTextColor(resources.getColor(R.color.issue_event_normal));
            break;
        case IssueEvent.TYPE_MERGED:
            message += String.format(resources.getString(R.string.issue_event_merged), "<b>" + event.getCommitId().substring(0,7) + "</b>");
            setText(0, TypefaceUtils.ICON_GIT_MERGE);
            textView(0).setTextColor(resources.getColor(R.color.issue_event_purple));
            break;
        case IssueEvent.TYPE_LOCKED:
            message += resources.getString(R.string.issue_event_lock);
            setText(0, TypefaceUtils.ICON_LOCK);
            textView(0).setTextColor(resources.getColor(R.color.issue_event_dark));
            break;
        case IssueEvent.TYPE_UNLOCKED:
            message += resources.getString(R.string.issue_event_unlock);
            setText(0, TypefaceUtils.ICON_KEY);
            textView(0).setTextColor(resources.getColor(R.color.issue_event_dark));
            break;
        case IssueEvent.TYPE_HEAD_REF_DELETED:
            message += resources.getString(R.string.issue_event_head_ref_deleted);
            setText(0, TypefaceUtils.ICON_GIT_BRANCH);
            textView(0).setTextColor(resources.getColor(R.color.issue_event_light));
            break;
        case IssueEvent.TYPE_HEAD_REF_RESTORED:
            message += resources.getString(R.string.issue_event_head_ref_restored);
            setText(0, TypefaceUtils.ICON_GIT_BRANCH);
            textView(0).setTextColor(resources.getColor(R.color.issue_event_light));
            break;
        }

        message += " " + TimeUtils.getRelativeTime(event.getCreatedAt());
        setText(1, Html.fromHtml(message));
    }

    protected void updateComment(final Comment comment) {
        imageGetter.bind(textView(0), comment.getBodyHtml(), comment.getId());
        avatars.bind(imageView(3), comment.getUser());

        setText(1, comment.getUser().getLogin());
        setText(2, TimeUtils.getRelativeTime(comment.getUpdatedAt()));

        // Edit Comment ImageButton
        if (editCommentListener != null) {
            view(4).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editCommentListener.onEditComment(comment);
                }
            });
        } else {
            view(4).setVisibility(View.GONE);
        }

        // Delete Comment ImageButton
        if (deleteCommentListener != null) {
            view(5).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteCommentListener.onDeleteComment(comment);
                }
            });
        } else {
            view(5).setVisibility(View.GONE);
        }
    }

    public MultiTypeAdapter setItems(Collection<?> items) {
        if (items == null || items.isEmpty())
            return this;
        return setItems(items.toArray());
    }

    public MultiTypeAdapter setItems(final Object[] items) {
        if (items == null || items.length == 0)
            return this;

        this.clear();

        for (Object item : items) {
            if(item instanceof Comment)
                this.addItem(0, item);
            else
                this.addItem(1, item);
        }

        notifyDataSetChanged();
        return this;
    }

    @Override
    protected View initialize(int type, View view) {
        view = super.initialize(type, view);

        if (type == 0) {
            textView(view, 0).setMovementMethod(LinkMovementMethod.getInstance());
            TypefaceUtils.setOcticons(textView(view, 4), textView(view, 5));
            setText(view, 4, TypefaceUtils.ICON_PENCIL);
            setText(view, 5, TypefaceUtils.ICON_X);
        } else {
            TypefaceUtils.setOcticons(textView(view, 0));
        }

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    protected int getChildLayoutId(int type) {
        if(type == 0)
            return R.layout.comment_item;
        else
            return R.layout.comment_event_item;
    }

    @Override
    protected int[] getChildViewIds(int type) {
        if(type == 0)
            return new int[] { R.id.tv_comment_body, R.id.tv_comment_author,
                    R.id.tv_comment_date, R.id.iv_avatar, R.id.iv_comment_edit, R.id.iv_comment_delete };
        else
            return new int[]{R.id.tv_event_icon, R.id.tv_event, R.id.iv_avatar};
    }
}
