package com.socialize.ui.test.comment;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;

import com.google.android.testing.mocking.AndroidMock;
import com.google.android.testing.mocking.UsesMocks;
import com.socialize.SocializeService;
import com.socialize.SocializeServiceImpl;
import com.socialize.android.ioc.IBeanFactory;
import com.socialize.auth.AuthProviderType;
import com.socialize.entity.Comment;
import com.socialize.entity.ListResult;
import com.socialize.error.SocializeException;
import com.socialize.listener.comment.CommentAddListener;
import com.socialize.listener.comment.CommentListListener;
import com.socialize.ui.SocializeUI;
import com.socialize.ui.auth.AuthRequestDialogFactory;
import com.socialize.ui.auth.AuthRequestListener;
import com.socialize.ui.comment.CommentAdapter;
import com.socialize.ui.comment.CommentAddButtonListener;
import com.socialize.ui.comment.CommentContentView;
import com.socialize.ui.comment.CommentEditField;
import com.socialize.ui.comment.CommentHeader;
import com.socialize.ui.comment.CommentListView;
import com.socialize.ui.comment.CommentScrollCallback;
import com.socialize.ui.comment.CommentScrollListener;
import com.socialize.ui.dialog.DialogFactory;
import com.socialize.ui.dialog.ProgressDialogFactory;
import com.socialize.ui.facebook.FacebookWallPoster;
import com.socialize.ui.test.SocializeUIActivityTest;
import com.socialize.ui.util.KeyboardUtils;
import com.socialize.ui.view.ViewFactory;
import com.socialize.util.Drawables;

public class CommentListViewTest extends SocializeUIActivityTest {
	
	@SuppressWarnings("unchecked")
	@UsesMocks ({
		ViewFactory.class,
		CommentEditField.class,
		CommentHeader.class,
		CommentContentView.class,
		Drawables.class,
		KeyboardUtils.class,
		CommentAdapter.class,
		Drawable.class,
		CommentAddButtonListener.class,
		CommentScrollListener.class,
		CommentScrollCallback.class
	})
	public void testInit() {
		final Context context = getContext();
		
		final String entityKey = "foobar";
		
		ViewFactory<CommentHeader> commentHeaderFactory = AndroidMock.createMock(ViewFactory.class);
		ViewFactory<CommentEditField> commentEditFieldFactory = AndroidMock.createMock(ViewFactory.class);
		ViewFactory<CommentContentView> commentContentViewFactory = AndroidMock.createMock(ViewFactory.class);
		
		CommentScrollCallback commentScrollCallback = AndroidMock.createMock(CommentScrollCallback.class);
		
		final CommentScrollListener onScrollListener = AndroidMock.createMock(CommentScrollListener.class, commentScrollCallback);
		final CommentAddButtonListener onClickListener = AndroidMock.createMock(CommentAddButtonListener.class, getContext());
		
		CommentEditField field = AndroidMock.createMock(CommentEditField.class, getContext());
		CommentHeader header = AndroidMock.createMock(CommentHeader.class, getContext());
		CommentContentView content = AndroidMock.createMock(CommentContentView.class, getContext());
		Drawables drawables = AndroidMock.createMock(Drawables.class);
		KeyboardUtils keyboardUtils = AndroidMock.createMock(KeyboardUtils.class);
		CommentAdapter commentAdapter = AndroidMock.createMock(CommentAdapter.class);
		Drawable drawable = AndroidMock.createMock(Drawable.class);
		
		AndroidMock.expect(commentHeaderFactory.make(context)).andReturn(header);
		AndroidMock.expect(commentEditFieldFactory.make(context)).andReturn(field);
		AndroidMock.expect(commentContentViewFactory.make(context)).andReturn(content);

		AndroidMock.expect(drawables.getDrawable("crosshatch.png", true, true, true)).andReturn(drawable);
		
		field.setButtonListener((OnClickListener) AndroidMock.anyObject());
		content.setListAdapter(commentAdapter);
		content.setScrollListener((OnScrollListener) AndroidMock.anyObject());
		
		AndroidMock.replay(commentHeaderFactory);
		AndroidMock.replay(commentEditFieldFactory);
		AndroidMock.replay(commentContentViewFactory);
		AndroidMock.replay(drawables);
		AndroidMock.replay(field);
		AndroidMock.replay(content);
		
		CommentListView view = new CommentListView(context, entityKey)  {
			
			int childIndex = 0;
			
			@Override
			public void addView(View child) {
				addResult(10+childIndex, child);
				childIndex++;
			}
			
			@Override
			public void setOrientation(int orientation) {
				addResult(0, orientation);
			}

			@Override
			public void setBackgroundDrawable(Drawable d) {
				addResult(1, d);
			}

			@Override
			public void setPadding(int left, int top, int right, int bottom) {
				addResult(2, left);
				addResult(3, top);
				addResult(4, right);
				addResult(5, bottom);
			}

			@Override
			protected CommentScrollListener getCommentScrollListener() {
				return onScrollListener;
			}

			@Override
			protected CommentAddButtonListener getCommentAddListener() {
				return onClickListener;
			}
		};
		
		view.setCommentHeaderFactory(commentHeaderFactory);
		view.setCommentEditFieldFactory(commentEditFieldFactory);
		view.setCommentContentViewFactory(commentContentViewFactory);
		view.setKeyboardUtils(keyboardUtils);
		view.setCommentAdapter(commentAdapter);
		view.setDrawables(drawables);
		
		view.init();
		
		assertEquals(LinearLayout.VERTICAL, getResult(0));
		assertSame(drawable, getResult(1));
		
		assertEquals(0, getResult(2));
		assertEquals(0, getResult(3));
		assertEquals(0, getResult(4));
		assertEquals(0, getResult(5));
		
		assertSame(header, getResult(10));
		assertSame(field, getResult(11));
		assertSame(content, getResult(12));
		
		AndroidMock.verify(commentHeaderFactory);
		AndroidMock.verify(commentEditFieldFactory);
		AndroidMock.verify(commentContentViewFactory);
		AndroidMock.verify(drawables);
		AndroidMock.verify(field);
		AndroidMock.verify(content);
		
	}
	
	public void testGetCommentScrollListener() {
		PublicCommentListView view = new PublicCommentListView(getContext()) {
			@Override
			public void getNextSet() {
				addResult(true);
			}
		};
		
		view.setLoading(true);
		
		CommentScrollListener commentScrollListener = view.getCommentScrollListener();
		
		assertNotNull(commentScrollListener.getCallback());
		
		commentScrollListener.getCallback().onGetNextSet();
		
		assertTrue(commentScrollListener.getCallback().isLoading());
		
		Boolean nextResult = getNextResult();
		
		assertNotNull(nextResult);
		assertTrue(nextResult);
	}
	
	@SuppressWarnings("unchecked")
	@UsesMocks ({
		IBeanFactory.class, 
		AuthRequestDialogFactory.class, 
		AuthRequestListener.class,
		SocializeService.class,
		SocializeUI.class,
		ProgressDialogFactory.class,
		ProgressDialog.class})
	public void testGetCommentAddListenerNotAuthed() {
		
		IBeanFactory<AuthRequestDialogFactory> authRequestDialogFactory = AndroidMock.createMock(IBeanFactory.class);
		AuthRequestDialogFactory authRequestDialog = AndroidMock.createMock(AuthRequestDialogFactory.class);
		ProgressDialogFactory progressDialogFactory = AndroidMock.createMock(ProgressDialogFactory.class);
		ProgressDialog dialog = AndroidMock.createMock(ProgressDialog.class, getContext());
		
		final AuthRequestListener listener = AndroidMock.createMock(AuthRequestListener.class);
		final SocializeService socializeService = AndroidMock.createMock(SocializeService.class);
		final SocializeUI socializeUI = AndroidMock.createMock(SocializeUI.class);
		
		AndroidMock.expect(authRequestDialogFactory.getBean()).andReturn(authRequestDialog);
		AndroidMock.expect(socializeService.isAuthenticated(AuthProviderType.FACEBOOK)).andReturn(false);
		AndroidMock.expect(progressDialogFactory.show(getContext(), "Posting comment", "Please wait...")).andReturn(dialog);
		AndroidMock.expect(socializeUI.isFacebookSupported()).andReturn(true);
		
		
		dialog.dismiss();
		authRequestDialog.show(getContext(), listener);
		
		AndroidMock.replay(authRequestDialogFactory);
		AndroidMock.replay(authRequestDialog);
		AndroidMock.replay(socializeService);
		AndroidMock.replay(socializeUI);
		
		PublicCommentListView view = new PublicCommentListView(getContext()) {
			
			@Override
			public void showError(Context context, Exception message) {
				addResult(0, message);
			}

			@Override
			public AuthRequestListener getCommentAuthListener(String text) {
				addResult(1, text);
				return listener;
			}

			@Override
			protected SocializeService getSocialize() {
				return socializeService;
			}
			
			@Override
			protected SocializeUI getSocializeUI() {
				return socializeUI;
			}
			
		};
		
		view.setAuthRequestDialogFactory(authRequestDialogFactory);
		view.setProgressDialogFactory(progressDialogFactory);
		
		CommentAddButtonListener commentScrollListener = view.getCommentAddListener();
		
		assertNotNull(commentScrollListener.getCallback());
		
		commentScrollListener.getCallback().onComment("foobar");
		commentScrollListener.getCallback().onError(getContext(), new Exception("foobar_error"));
		
		AndroidMock.verify(authRequestDialogFactory);
		AndroidMock.verify(authRequestDialog);
		AndroidMock.verify(socializeService);
		AndroidMock.verify(socializeUI);
		
		Exception message = getResult(0);
		String text = getResult(1);
		
		assertNotNull(message);
		assertNotNull(text);
		
		assertEquals("foobar", text);
		assertEquals("foobar_error", message.getMessage());
	}
	
	@UsesMocks ({SocializeService.class})
	public void testGetCommentAddListenerAuthed() {
		
		final SocializeService socializeService = AndroidMock.createMock(SocializeService.class);
		
		AndroidMock.expect(socializeService.isAuthenticated(AuthProviderType.FACEBOOK)).andReturn(true);
		AndroidMock.replay(socializeService);
		
		PublicCommentListView view = new PublicCommentListView(getContext()) {
			
			@Override
			public void doPostComment(String comment) {
				addResult(0, comment);
			}
			
			@Override
			public void showError(Context context, Exception message) {
				addResult(1, message);
			}

			@Override
			protected SocializeService getSocialize() {
				return socializeService;
			}
		};
		
		CommentAddButtonListener commentScrollListener = view.getCommentAddListener();
		
		assertNotNull(commentScrollListener.getCallback());
		
		commentScrollListener.getCallback().onComment("foobar");
		commentScrollListener.getCallback().onError(getContext(), new Exception("foobar_error"));
		
		AndroidMock.verify(socializeService);
		
		String comment = getResult(0);
		Exception message = getResult(1);
		
		assertNotNull(comment);
		assertNotNull(message);
		
		assertEquals("foobar", comment);
		assertEquals("foobar_error", message.getMessage());
	}
	
	@SuppressWarnings("unchecked")
	@UsesMocks ({
		Comment.class, 
		ProgressDialog.class,
		DialogFactory.class,
		CommentAdapter.class,
		List.class,
		CommentEditField.class,
		CommentHeader.class,
		CommentContentView.class,
		FacebookWallPoster.class})
	public void testPostCommentSuccess() {
		
		final String title = "Posting comment";
		final String message = "Please wait...";
		final int totalCount = 69;
		final int startIndex = 0;
		final int endIndex = 10;
		final String commentString = "foobar_comment";
		
		final String entityKey = "foobar_entity_key";
		final String entityName = "foobar_entity_name";
		
		final Comment comment = AndroidMock.createMock(Comment.class);
		final ProgressDialog dialog = AndroidMock.createMock(ProgressDialog.class, getActivity());
		final DialogFactory<ProgressDialog> progressDialogFactory = AndroidMock.createMock(DialogFactory.class);
		final CommentAdapter commentAdapter = AndroidMock.createMock(CommentAdapter.class);
		final List<Comment> comments = AndroidMock.createMock(List.class);
		final CommentEditField field = AndroidMock.createMock(CommentEditField.class, getContext());
		final CommentHeader header = AndroidMock.createMock(CommentHeader.class, getContext());
		final CommentContentView content = AndroidMock.createMock(CommentContentView.class, getContext());
		
		final FacebookWallPoster facebookWallPoster = AndroidMock.createMock(FacebookWallPoster.class);

		facebookWallPoster.postComment(getActivity(), entityKey, entityName, commentString, true, null);
		
//		dialog.setTitle(title);
//		dialog.setMessage(message);
//		dialog.show();
		
		AndroidMock.expect(progressDialogFactory.show(getContext(), title, message)).andReturn(dialog);
		AndroidMock.expect(commentAdapter.getComments()).andReturn(comments);

		comments.add(0, comment);
		header.setText((totalCount+1) + " Comments");
		field.clear();
		commentAdapter.notifyDataSetChanged();
		content.scrollToTop();
		dialog.dismiss();
		
		AndroidMock.replay(progressDialogFactory);
		AndroidMock.replay(commentAdapter);
		AndroidMock.replay(comments);
		AndroidMock.replay(header);
		AndroidMock.replay(field);
		AndroidMock.replay(content);
		AndroidMock.replay(dialog);
		AndroidMock.replay(facebookWallPoster);
		
		
		// Because of the use of an anonymous inner class as the callback
		// we need to override the SocializeService instance to capture the callback
		// class and call it directly
		
		final SocializeService socialize = new SocializeServiceImpl() {
			@Override
			public void addComment(String url, String str, CommentAddListener commentAddListener) {
				// call onCreate manually for the test.
				assertEquals(commentString, str);
				commentAddListener.onCreate(comment);
			}
			
			@Override
			public boolean isAuthenticated(AuthProviderType providerType) {
				return providerType.equals(AuthProviderType.FACEBOOK);
			}
		};
		
		PublicCommentListView view = new PublicCommentListView(getContext()) {
			@Override
			protected SocializeService getSocialize() {
				return socialize;
			}
		};
		
		view.setCommentAdapter(commentAdapter);
		view.setProgressDialogFactory(progressDialogFactory);
		view.setHeader(header);
		view.setField(field);
		view.setContent(content);
		view.setStartIndex(startIndex);
		view.setEndIndex(endIndex);
		view.setTotalCount(totalCount);
		view.setFacebookWallPoster(facebookWallPoster);
		view.setEntityKey(entityKey);
		view.setEntityName(entityName);
		view.setUseLink(true);
		
		view.doPostComment(commentString);
		
		AndroidMock.verify(progressDialogFactory);
		AndroidMock.verify(commentAdapter);
		AndroidMock.verify(comments);
		AndroidMock.verify(header);
		AndroidMock.verify(field);
		AndroidMock.verify(content);
		AndroidMock.verify(dialog);
		AndroidMock.verify(facebookWallPoster);
		
		// Make sure indexes were updated
		assertEquals(totalCount+1, view.getTotalCount());
		assertEquals(startIndex+1, view.getStartIndex());
		assertEquals(endIndex+1, view.getEndIndex());
	}
	
	@SuppressWarnings("unchecked")
	@UsesMocks ({
		ProgressDialog.class,
		DialogFactory.class,
		SocializeException.class,
		FacebookWallPoster.class})
	public void testPostCommentFail() {
		
		final String title = "Posting comment";
		final String message = "Please wait...";
		final String comment = "foobar";
		
		final String entityKey = "foobar_entity_key";
		final String entityName = "foobar_entity_name";
		
		final SocializeException error = AndroidMock.createMock(SocializeException.class);
		final ProgressDialog dialog = AndroidMock.createMock(ProgressDialog.class, getContext());
		final DialogFactory<ProgressDialog> progressDialogFactory = AndroidMock.createMock(DialogFactory.class);
		final FacebookWallPoster facebookWallPoster = AndroidMock.createMock(FacebookWallPoster.class);

		dialog.dismiss();
//		dialog.setTitle(title);
//		dialog.setMessage(message);
//		dialog.show();
		
		AndroidMock.expect(progressDialogFactory.show(getContext(), title, message)).andReturn(dialog);
		
		facebookWallPoster.postComment(getActivity(), entityKey, entityName, comment, true, null);
		
		AndroidMock.replay(progressDialogFactory);
		AndroidMock.replay(dialog);
		AndroidMock.replay(error);
		AndroidMock.replay(facebookWallPoster);
		
		// Because of the use of an anonymous inner class as the callback
		// we need to override the SocializeService instance to capture the callback
		// class and call it directly
		
		final SocializeService socialize = new SocializeServiceImpl() {
			@Override
			public void addComment(String url, String str, CommentAddListener commentAddListener) {
				// call onError manually for the test.
				commentAddListener.onError(error);
			}

			@Override
			public boolean isAuthenticated(AuthProviderType providerType) {
				return providerType.equals(AuthProviderType.FACEBOOK);
			}
		};
		
		PublicCommentListView view = new PublicCommentListView(getContext()) {
			@Override
			protected SocializeService getSocialize() {
				return socialize;
			}

			@Override
			public void showError(Context context, Exception message) {
				// Expect this
				addResult(message);
			}
		};
		
		view.setProgressDialogFactory(progressDialogFactory);
		view.setFacebookWallPoster(facebookWallPoster);
		view.setEntityKey(entityKey);
		view.setEntityName(entityName);
		view.setUseLink(true);
		
		view.doPostComment(comment);
		
		AndroidMock.verify(progressDialogFactory);
		AndroidMock.verify(dialog);
		AndroidMock.verify(error);
		AndroidMock.verify(facebookWallPoster);
		
		Exception result = getNextResult();
		
		assertNotNull(result);
		
	}
	

	@UsesMocks ({CommentAdapter.class})
	public void testGetNextSetIsLast() {
		
		final CommentAdapter commentAdapter = AndroidMock.createMock(CommentAdapter.class);
		
		commentAdapter.notifyDataSetChanged();
		commentAdapter.setLast(true);
		
		AndroidMock.replay(commentAdapter);
		
		PublicCommentListView view = new PublicCommentListView(getContext()) {
			@Override
			protected SocializeService getSocialize() {
				fail(); // Shouldn't be called
				return null;
			}
		};
		
		// Orchestrate the completion state
		final int totalCount = 69;
		final int endIndex = 70;
		final int startIndex = 60;
		final int grabLength = 10;
		
		view.setCommentAdapter(commentAdapter);
		view.setStartIndex(startIndex);
		view.setEndIndex(endIndex);
		view.setTotalCount(totalCount);
		view.setDefaultGrabLength(grabLength);
		
		
		view.getNextSet();
		
		AndroidMock.verify(commentAdapter);
		
		assertEquals(totalCount, view.getEndIndex());
		assertEquals(startIndex+grabLength, view.getStartIndex());
		assertFalse(view.isLoading());
	}
	
	@SuppressWarnings("unchecked")
	@UsesMocks ({
		CommentAdapter.class,
		List.class,
		ListResult.class})
	public void testGetNextSet() {
		
		final int totalCount = 169;
		final int startIndex = 0;
		final int endIndex = 70;
		
		final CommentAdapter commentAdapter = AndroidMock.createMock(CommentAdapter.class);
		final List<Comment> comments = AndroidMock.createMock(List.class);
		final List<Comment> listResultComments = AndroidMock.createMock(List.class);
		
		final ListResult<Comment> entities = AndroidMock.createMock(ListResult.class);

		AndroidMock.expect(commentAdapter.getComments()).andReturn(comments);
		AndroidMock.expect(entities.getItems()).andReturn(listResultComments);
		AndroidMock.expect(comments.addAll(listResultComments)).andReturn(true);
		
		commentAdapter.setComments(comments);
		commentAdapter.notifyDataSetChanged();
		
		AndroidMock.replay(entities);
		AndroidMock.replay(commentAdapter);
		AndroidMock.replay(comments);
		
		// Because of the use of an anonymous inner class as the callback
		// we need to override the SocializeService instance to capture the callback
		// class and call it directly
		final SocializeService socialize = new SocializeServiceImpl() {
			@Override
			public void listCommentsByEntity(String url, int startIndex, int endIndex, CommentListListener commentListListener) {
				commentListListener.onList(entities);
			}
		};
		
		PublicCommentListView view = new PublicCommentListView(getContext()) {
			@Override
			protected SocializeService getSocialize() {
				return socialize;
			}
		};
		
		view.setCommentAdapter(commentAdapter);
		view.setStartIndex(startIndex);
		view.setEndIndex(endIndex);
		view.setTotalCount(totalCount);
		view.setDefaultGrabLength(10);
		
		view.getNextSet();
		
		AndroidMock.verify(commentAdapter);
		AndroidMock.verify(comments);
		AndroidMock.verify(entities);
		
		assertFalse(view.isLoading());
	}
	
	

	@SuppressWarnings("unchecked")
	@UsesMocks ({
		CommentAdapter.class,
		List.class,
		CommentHeader.class,
		CommentContentView.class,
		ListResult.class})
	public void testDoListCommentsSuccessEmptyCommentsWithoutUpdate() {
		
		final int totalCount = 69;
		final int startIndex = 0;
		final int endIndex = 70;
		
		final CommentAdapter commentAdapter = AndroidMock.createMock(CommentAdapter.class);
		final List<Comment> comments = AndroidMock.createMock(List.class);
		final List<Comment> listResultComments = AndroidMock.createMock(List.class);
		
		final CommentHeader header = AndroidMock.createMock(CommentHeader.class, getContext());
		final CommentContentView content = AndroidMock.createMock(CommentContentView.class, getContext());
		final ListResult<Comment> entities = AndroidMock.createMock(ListResult.class);
		
		AndroidMock.expect(commentAdapter.getComments()).andReturn(comments);
		AndroidMock.expect(comments.size()).andReturn(0); // Empty comments
		AndroidMock.expect(entities.getItems()).andReturn(listResultComments);
		AndroidMock.expect(entities.getTotalCount()).andReturn(totalCount);

		commentAdapter.setComments(listResultComments);
		commentAdapter.setLast(true);
		header.setText(totalCount + " Comments");
		content.showList();
		
		AndroidMock.replay(entities);
		AndroidMock.replay(commentAdapter);
		AndroidMock.replay(comments);
		AndroidMock.replay(header);
		AndroidMock.replay(content);
		
		// Because of the use of an anonymous inner class as the callback
		// we need to override the SocializeService instance to capture the callback
		// class and call it directly
		final SocializeService socialize = new SocializeServiceImpl() {
			@Override
			public void listCommentsByEntity(String url, int startIndex, int endIndex, CommentListListener commentListListener) {
				commentListListener.onList(entities);
			}
		};
		
		PublicCommentListView view = new PublicCommentListView(getContext()) {
			@Override
			protected SocializeService getSocialize() {
				return socialize;
			}
		};
		
		view.setCommentAdapter(commentAdapter);
		view.setHeader(header);
		view.setContent(content);
		view.setStartIndex(startIndex);
		view.setDefaultGrabLength(endIndex);
		
		view.doListComments(false);
		
		AndroidMock.verify(commentAdapter);
		AndroidMock.verify(comments);
		AndroidMock.verify(header);
		AndroidMock.verify(content);
		AndroidMock.verify(entities);
		
		assertEquals(totalCount, view.getTotalCount());
		assertFalse(view.isLoading());
	}
	
	
	@SuppressWarnings("unchecked")
	@UsesMocks ({
		SocializeException.class,
		CommentAdapter.class,
		List.class})
	public void testDoListCommentsFailEmptyCommentsWithoutUpdate() {
		
		final SocializeException error = AndroidMock.createMock(SocializeException.class);
		final CommentAdapter commentAdapter = AndroidMock.createMock(CommentAdapter.class);
		final List<Comment> comments = AndroidMock.createMock(List.class);
		final CommentContentView content = AndroidMock.createMock(CommentContentView.class, getContext());
		
		content.showList();
		AndroidMock.expect(commentAdapter.getComments()).andReturn(comments);
		AndroidMock.expect(comments.size()).andReturn(0); // Empty comments
		
		AndroidMock.replay(commentAdapter);
		AndroidMock.replay(comments);
		AndroidMock.replay(content);
		AndroidMock.replay(error);
		
		// Because of the use of an anonymous inner class as the callback
		// we need to override the SocializeService instance to capture the callback
		// class and call it directly
		final SocializeService socialize = new SocializeServiceImpl() {
			@Override
			public void listCommentsByEntity(String url, int startIndex, int endIndex, CommentListListener commentListListener) {
				commentListListener.onError(error);
			}
		};
		
		PublicCommentListView view = new PublicCommentListView(getContext()) {
			@Override
			protected SocializeService getSocialize() {
				return socialize;
			}

			@Override
			public void showError(Context context, Exception error) {
				addResult(error);
			}
		};
		
		view.setCommentAdapter(commentAdapter);
		view.setContent(content);
		view.doListComments(false);
		
		AndroidMock.verify(commentAdapter);
		AndroidMock.verify(comments);
		AndroidMock.verify(error);
		AndroidMock.verify(content);
		
		Exception result = getNextResult();
		
		assertNotNull(result);
	}
	
	@SuppressWarnings("unchecked")
	@UsesMocks ({
		CommentAdapter.class,
		List.class,
		CommentContentView.class})
	public void testDoListCommentsSuccessPopulatedCommentsWithoutUpdate() {
		
		final CommentAdapter commentAdapter = AndroidMock.createMock(CommentAdapter.class);
		final List<Comment> comments = AndroidMock.createMock(List.class);
		final CommentContentView content = AndroidMock.createMock(CommentContentView.class, getContext());
		
		AndroidMock.expect(commentAdapter.getComments()).andReturn(comments);
		AndroidMock.expect(comments.size()).andReturn(10); // Populated comments

		commentAdapter.notifyDataSetChanged();
		content.showList();
		
		AndroidMock.replay(commentAdapter);
		AndroidMock.replay(comments);
		AndroidMock.replay(content);
		
		PublicCommentListView view = new PublicCommentListView(getContext());
		
		view.setCommentAdapter(commentAdapter);
		view.setContent(content);
		
		view.doListComments(false);
		
		AndroidMock.verify(commentAdapter);
		AndroidMock.verify(comments);
		AndroidMock.verify(content);
		
		assertFalse(view.isLoading());
	}
	

	@UsesMocks ({SocializeService.class})
	public void testtestOnViewLoadSuccess() {
		final SocializeService socialize = AndroidMock.createMock(SocializeService.class);
		
		AndroidMock.expect(socialize.isAuthenticated()).andReturn(true);
		AndroidMock.replay(socialize);
		
		PublicCommentListView view = new PublicCommentListView(getContext()) {
			@Override
			protected SocializeService getSocialize() {
				return socialize;
			}

			@Override
			public void doListComments(boolean update) {
				addResult(update);
			}
		};
		
		view.onViewLoad();
		
		Boolean update = getNextResult();
		
		assertNotNull(update);
		assertFalse(update);
		
		AndroidMock.verify(socialize);
	}
	
	@UsesMocks ({
		SocializeService.class,
		CommentContentView.class})
	public void testOnViewLoadFail() {
		final SocializeService socialize = AndroidMock.createMock(SocializeService.class);
		final CommentContentView content = AndroidMock.createMock(CommentContentView.class, getContext());
		
		AndroidMock.expect(socialize.isAuthenticated()).andReturn(false);
		content.showList();
		
		AndroidMock.replay(socialize);
		AndroidMock.replay(content);
		
		PublicCommentListView view = new PublicCommentListView(getContext()) {
			@Override
			protected SocializeService getSocialize() {
				return socialize;
			}

			@Override
			public void doListComments(boolean update) {
				fail();
			}

			@Override
			public void showError(Context context, Exception error) {
				addResult(error);
			}
			
			
		};
		
		view.setContent(content);
		view.onViewLoad();
		
		Exception error = getNextResult();
		
		assertNotNull(error);
		assertEquals("Socialize not authenticated", error.getMessage());
		
		AndroidMock.verify(socialize);
		AndroidMock.verify(content);
	}
	
	class PublicCommentListView extends CommentListView {

		public PublicCommentListView(Context context) {
			super(context);
		}

		@Override
		public CommentScrollListener getCommentScrollListener() {
			return super.getCommentScrollListener();
		}

		@Override
		public CommentAddButtonListener getCommentAddListener() {
			return super.getCommentAddListener();
		}

		@Override
		public AuthRequestListener getCommentAuthListener(String text) {
			return super.getCommentAuthListener(text);
		}

		@Override
		public void setLoading(boolean loading) {
			super.setLoading(loading);
		}

		@Override
		public void setField(CommentEditField field) {
			super.setField(field);
		}

		@Override
		public void setHeader(CommentHeader header) {
			super.setHeader(header);
		}

		@Override
		public void setContent(CommentContentView content) {
			super.setContent(content);
		}

		@Override
		public void setStartIndex(int startIndex) {
			super.setStartIndex(startIndex);
		}

		@Override
		public void setEndIndex(int endIndex) {
			super.setEndIndex(endIndex);
		}

		@Override
		public void setTotalCount(int totalCount) {
			super.setTotalCount(totalCount);
		}

		@Override
		public void getNextSet() {
			super.getNextSet();
		}

		@Override
		public void onAttachedToWindow() {
			super.onAttachedToWindow();
		}

		@Override
		public void onViewUpdate() {
			super.onViewUpdate();
		}

		@Override
		public void onViewLoad() {
			super.onViewLoad();
		}
	}
}