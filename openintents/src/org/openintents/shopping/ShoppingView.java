/* 
 * Copyright (C) 2007 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.shopping;

import java.util.ArrayList;

import org.openintents.R;
import org.openintents.provider.Shopping;
import org.openintents.provider.Shopping.ContainsFull;
import org.openintents.provider.Shopping.Lists;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class ShoppingView extends Activity //implements AdapterView.OnItemClickListener 
{

	/**
	 * TAG for logging.
	 */
	private static final String TAG = "ShoppingProvider";
	
	private static final int MENU_NEW_LIST = Menu.FIRST;
	private static final int MENU_CLEAN_UP_LIST = Menu.FIRST + 1;
	private static final int MENU_DELETE_LIST = Menu.FIRST + 2;
	
	// TODO: Implement the following menu items
	private static final int MENU_EDIT_LIST = Menu.FIRST + 3; // includes rename
	private static final int MENU_EDIT_ITEM = Menu.FIRST + 4; // includes rename
	private static final int MENU_DELETE_ITEM = Menu.FIRST + 5;
	private static final int MENU_SORT = Menu.FIRST + 6; // sort alphabetically or modified
	private static final int MENU_PICK_ITEMS = Menu.FIRST + 7; // pick from previously used items
	
	// TODO: Further possible actions to implement:
	// * Move items to some other shopping list
	
	/**
	 * Private members connected to Spinner ListFilter
	 */
	private Spinner mSpinnerListFilter;
	private Cursor mCursorListFilter;
	private static final String[] mStringListFilter = 
		new String[] { Lists._ID, Lists.NAME, Lists.IMAGE};
	private static final int mStringListFilterID = 0;
	private static final int mStringListFilterNAME = 1;
	private static final int mStringListFilterIMAGE = 2;
	
	ListView mListItems;
	Cursor mCursorItems;
	private String TEST;
	private static final String[] mStringItems =
		new String[] { 
				ContainsFull._ID, 
				ContainsFull.ITEM_NAME,
				ContainsFull.ITEM_IMAGE,
				ContainsFull.STATUS};
	private static final int mStringItemsCONTAINSID = 0;
	private static final int mStringItemsITEMNAME = 1;
	private static final int mStringItemsITEMIMAGE = 2;
	private static final int mStringItemsSTATUS = 3;
	
	EditText mEditText;
	
	protected Context mDialogContext;
	protected Dialog mDialog;
	
	// TODO: Set up state information for onFreeze(), ...
	// State data to be stored when freezing:
	private final String ORIGINAL_ITEM = "original item";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		//setTheme(android.R.style.Theme_White);
		//setTheme(android.R.style.Theme_Dialog);
		//setTheme(android.R.style.Theme_Dark);
		//setTheme(android.R.style.Theme_Black);
		setContentView(R.layout.shopping);

		// hook up all buttons, lists, edit text:
		createView();
		
		// populate the lists
		fillListFilter();
		fillItems();
		
		if (icicle != null) {
			String prevText = icicle.getString(ORIGINAL_ITEM);
			if (prevText != null) {
				mEditText.setText(prevText);
			}
		}
		
		// set focus to the edit line:
		mEditText.requestFocus();
	}
	
	@Override
	protected void onFreeze(Bundle outState) {
		super.onFreeze(outState);
		
        // Save original text from edit box
		String s = mEditText.getText().toString();
        outState.putString(ORIGINAL_ITEM, s);
    }

	/**
	 * Hook up buttons, lists, and edittext with functionality.
	 */
	private void createView() {
		mSpinnerListFilter = (Spinner) findViewById(R.id.spinner_listfilter);
		mSpinnerListFilter.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView parent, View v,
					int position, long id) {
				fillItems();
			}
			
			public void onNothingSelected(AdapterView arg0) {
				fillItems();
			}
		});
        
		mEditText = (EditText) findViewById(R.id.edittext_add_item);
		mEditText.setKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent key) {
				//Log.i(TAG, "KeyCode: " + keyCode 
				//		+ " =?= " 
				//		+Integer.parseInt(getString(R.string.key_return)) );
				
				// Shortcut: Instead of pressing the button, 
				// one can also press the "Enter" key.
				if (key.isDown() && 
						keyCode == Integer.parseInt(getString(R.string.key_return)))
				{
					insertNewItem();
					return true;
				};
				return false;
			}
		});
		
		Button button = (Button) findViewById(R.id.button_add_item);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				insertNewItem();
			}
		});
		
		mListItems = (ListView) findViewById(R.id.list_items);		
		mListItems.setOnItemClickListener(
			new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView parent, 
						View v, int pos, long id) {
					Cursor c = (Cursor) parent.obtainItem(pos);
					toggleItemBought(c);
				}
				
		});
		mListItems.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView parent, View v,
					int position, long id) {
				// Log.i(TAG, "mListItems selected: pos:" 
				// 	+ position + ", id:" + id);
			}
			@Override
			public void onNothingSelected(AdapterView arg0) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	/**
	 * Inserts new item from edit box into 
	 * currently selected shopping list.
	 */
	private void insertNewItem() {
		EditText edittext = 
			(EditText) findViewById(R.id.edittext_add_item);
						
		String newItem = edittext.getText().toString();
		
		// Only add if there is something to add:
		if (newItem.compareTo("") != 0) {
			long listId = getSelectedListId();
			
			long itemId = Shopping.insertItem(getContentResolver(), 
					newItem);
			
			Log.i(TAG, "Insert new item. " 
					+ " itemId = " + itemId + ", listId = " + listId);
			Shopping.insertContains(getContentResolver(), 
					itemId, listId);
			
			edittext.setText("");
			
			fillItems();
		}
	}
	
	// strike item through or undo this.
	private void toggleItemBought(Cursor c) {
		// Toggle status:
		long oldstatus = c.getLong(mStringItemsSTATUS);
		long newstatus = Shopping.Status.BOUGHT;
		if (oldstatus == Shopping.Status.BOUGHT) {
			newstatus = Shopping.Status.WANT_TO_BUY;
		}
			
		c.updateLong(mStringItemsSTATUS, newstatus);
		
		Log.i(TAG, "Commit now:");
		c.commitUpdates();
		
		Log.i(TAG, "Requery now:");
		c.requery();
		
		// fillItems();
	}
	
	// Menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Standard menu
		menu.add(0, MENU_NEW_LIST, R.string.new_list)
			.setShortcut(KeyEvent.KEYCODE_0, 0, KeyEvent.KEYCODE_N);
		menu.add(0, MENU_CLEAN_UP_LIST, R.string.clean_up_list)
			.setShortcut(KeyEvent.KEYCODE_1, 0, KeyEvent.KEYCODE_C);
		menu.add(0, MENU_DELETE_LIST, R.string.delete_list)
		.setShortcut(KeyEvent.KEYCODE_2, 0, KeyEvent.KEYCODE_D);
	
		// Generate any additional actions that can be performed on the
        // overall list.  This allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.ALTERNATIVE_CATEGORY);
        menu.addIntentOptions(
            Menu.ALTERNATIVE, 0, new ComponentName(this, ShoppingView.class),
            null, intent, 0, null);
        
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		// TODO: Add item-specific menu items (see NotesList.java example)
		// like edit, strike-through, delete.
		
		// Delete list is possible, if we have more than one list:
		menu.setItemShown(MENU_DELETE_LIST, mCursorListFilter.count() > 1);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(Item item) {
		switch (item.getId()) {
		case MENU_NEW_LIST:
			newListDialog();
			return true;
			
		case MENU_CLEAN_UP_LIST:
			cleanupList();
			return true;
		
		case MENU_DELETE_LIST:
			deleteListConfirm();
			return true;
		}
		return super.onOptionsItemSelected(item);
		
	}
	
	///////////////////////////////////////////////////////
	//
	// Menu functions
	//

	/**
	 * Opens a dialog to add a new shopping list.
	 */
	private void newListDialog() {
		
		// TODO Shall we implement this as action?
		// Then other applications may call this as well.

		mDialog = new Dialog(ShoppingView.this);
		
		mDialog.setContentView(R.layout.input_box);
		
		mDialog.setTitle(getString(R.string.ask_new_list));
		
		EditText et = (EditText) mDialog.findViewById(R.id.edittext);
		et.setText(getString(R.string.new_list));
		et.selectAll();
		
		// Accept OK also when user hits "Enter"
		et.setKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(final View v, final int keyCode, 
					final KeyEvent key) {
				//Log.i(TAG, "KeyCode: " + keyCode);
				
				if (key.isDown() && keyCode == Integer
							.parseInt(getString(R.string.key_return))) {
					// User pressed "Enter" 
					EditText edittext = (EditText) 
						mDialog.findViewById(R.id.edittext);
					
					Shopping.insertList(getContentResolver(), 
						edittext.getText().toString());
					
					edittext.setText("");
					fillListFilter();
					
					mDialog.dismiss();
					return true;	
				}
				return false;
			}
			
		});
		
		
		Button bOk = (Button) mDialog.findViewById(R.id.ok);
		bOk.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				EditText edittext = (EditText) mDialog
					.findViewById(R.id.edittext);
				
				Shopping.insertList(getContentResolver(), 
						edittext.getText().toString());
				
				edittext.setText("");
				fillListFilter();
				
				mDialog.dismiss();
			}
		});
		
		Button bCancel = (Button) mDialog.findViewById(R.id.cancel);
		bCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mDialog.cancel();
			}
		});
		
		mDialog.show();
	}
	
	/**
	 * Clean up the currently visible shopping list
	 * by removing items from list that are marked BOUGHT.
	 */
	private void cleanupList() {
		// Delete all items from current list 
		// which have STATUS = Status.BOUGHT
		
		// TODO One could write one SQL statement to delete all at once.
		// But as long as shopping lists stay small, it should not matter.
		
		boolean nothingdeleted = true;
		mCursorItems.moveTo(-1); // move to beginning
		while (mCursorItems.next())
		{
			if (mCursorItems.getLong(mStringItemsSTATUS)
				== Shopping.Status.BOUGHT) {
				mCursorItems.deleteRow();
				mCursorItems.prev(); // Otherwise we would skip an item
				nothingdeleted = false;
			}
		}
		
		if (nothingdeleted) {
			// Show dialog:

			AlertDialog.show(ShoppingView.this, 
				getString(R.string.clean_up_list),
				getString(R.string.no_items_marked), 
				getString(R.string.ok),
				false);
			
		}
	}

	/**
	 * Confirm 'delete list' command by AlertDialog.
	 */
	private void deleteListConfirm() {
		AlertDialog.show(ShoppingView.this, 
			getString(R.string.delete_list),
			getString(R.string.confirm_delete_list), 
			getString(R.string.ok),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface di, int whichDialog) {
					Log.i(TAG, "Dialog click on:" + whichDialog);
					deleteList();
				}
			},
			getString(R.string.cancel),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface di, int whichDialog) {
					Log.i(TAG, "Dialog click on:" + whichDialog);
				}
			},
			true, 
			new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface di) {
					// TODO Auto-generated method stub
				}				
			});
		
	}
	
	/**
	 * Deletes currently selected shopping list.
	 */
	private void deleteList() {
		// First delete all items in list
		mCursorItems.moveTo(0); // move to beginning
		while (mCursorItems.count() > 0) {
			mCursorItems.deleteRow();
		}
		
		// Then delete currently selected list
		mCursorListFilter.deleteRow();
		
		// Update view
		fillListFilter();
		fillItems();
	}
	
	///////////////////////////////////////////////////////
	//
	// Helper functions
	//
	/**
	 * Returns the ID of the selected shopping list.
	 * @return ID of selected shopping list.
	 */
	private long getSelectedListId() {
		// Obtain Id of currently selected shopping list:
		mCursorListFilter.moveTo(
				mSpinnerListFilter.getSelectedItemIndex());
		return mCursorListFilter.getLong(mStringListFilterID);
	};
	
	/**
	 * 
	 */
	private void fillListFilter() {
		// Get a cursor with all lists
		mCursorListFilter = getContentResolver().query(Lists.CONTENT_URI, 
				mStringListFilter, 
				null, null, Lists.DEFAULT_SORT_ORDER);
		startManagingCursor(mCursorListFilter);

		if (mCursorListFilter == null) {
			Log.e(TAG, "missing shopping provider");
			
			mSpinnerListFilter.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { getString(R.string.no_shopping_provider) }));
			return;
		}
		
		if (mCursorListFilter.count() < 1) {
			// We have to create default shopping list:
			// TODO Put the following string into resource my_shopping_list
			Shopping.insertList(getContentResolver(), "My shopping list");
			
			// TODO Check if insertion really worked. Otherwise
			//      we may end up in infinite recursion.
			
			// The insertion should have worked, so let us call ourselves
			// to try filling the list again:
			fillListFilter();
			return;
		}

		ArrayList<String> list = new ArrayList<String>();
		// TODO Create summary of all lists
		// list.add(ALL);
		while (mCursorListFilter.next()) {
			list.add(mCursorListFilter.getString(mStringListFilterNAME));
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
		mSpinnerListFilter.setAdapter(adapter);
		
	}


	private void fillItems() {
		Log.i(TAG, "Starting fillItems()");
		
		long listId = getSelectedListId();
		
		// Get a cursor for all items that are contained 
		// in currently selected shopping list.
		mCursorItems = getContentResolver().query(
				ContainsFull.CONTENT_URI,
				mStringItems,
				"list_id = " + listId, null,
				ContainsFull.DEFAULT_SORT_ORDER);
		startManagingCursor(mCursorItems);
		

		if (mCursorItems == null) {
			Log.e(TAG, "missing shopping provider");
			mListItems.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no shopping provider" }));
			return;
		}
		
		ListAdapter adapter = new mSimpleCursorAdapter(this,
				// Use a template that displays a text view
				R.layout.shopping_item_row,
				// Give the cursor to the list adapter
				mCursorItems,
				// Map the IMAGE and NAME to...
				new String[] { 
					ContainsFull.ITEM_NAME, 
					ContainsFull.ITEM_IMAGE },
				// the view defined in the XML template
				new int[] { 
					R.id.name, 
					R.id.image_URI });
		mListItems.setAdapter(adapter);
		
		TEST = new String("ok");
		Log.i(TAG, "fillItems: mCursorItems : " + (mCursorItems == null) 
				+ ", " + TEST);
		
		//strikeItems();
	}
		
	/**
	 * 
	 * Extend the SimpleCursorAdapter to strike through items.
	 * if STATUS == Shopping.Status.BOUGHT
	 * 
	 */
	public class mSimpleCursorAdapter extends SimpleCursorAdapter {

		/**
		 * Constructor simply calls super class.
		 * @param context Context.
		 * @param layout Layout.
		 * @param c Cursor.
		 * @param from Projection from.
		 * @param to Projection to.
		 */
		mSimpleCursorAdapter(final Context context, final int layout, 
				final Cursor c, final String[] from, final int[] to) {
			super(context, layout, c, from, to);
		}
		
		/**
		 * Additionally to the standard bindView, we also
		 * check for STATUS, and strike the item through if BOUGHT.
		 */
		@Override
		public void bindView(final View view, final Context context, 
				final Cursor cursor) {
			//Log.i(TAG, "bindView " + view.toString());
			super.bindView(view, context, cursor);
			
			TextView t = (TextView) view.findViewById(R.id.name);
			if (cursor.getLong(mStringItemsSTATUS) 
					== Shopping.Status.BOUGHT) {
				// We have bought the item,
				// so we strike it through:
				
				// First convert text to 'spannable'
				t.setText(t.getText(), TextView.BufferType.SPANNABLE);
				Spannable str = (Spannable) t.getText();
				
				// Strikethrough
				str.setSpan(new StrikethroughSpan(), 0, str.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				
				// apply color
				// TODO: How to get color from resource?
				//Drawable colorStrikethrough = context
				//	.getResources().getDrawable(R.drawable.strikethrough);
				str.setSpan(new ForegroundColorSpan(0xFF006600), 0,
						str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				// color: 0x33336600
				
			}
		}
		
	}
}

