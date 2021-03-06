/* 
 * Copyright (C) 2008-2009 OpenIntents.org
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

package org.openintents.localebridge.util;

import java.util.List;

import org.openintents.intents.AutomationIntents;
import org.openintents.localebridge.LogConstants;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class AutomationUtils {
	private static final String TAG = LogConstants.TAG;
	private static final boolean debug = LogConstants.debug;
	
	public static String EXTRA_RUN_AUTOMATION_COMPONENT = "org.openintents.internal.extra.RUN_AUTOMATION_COMPONENT";

	public static String ACTION_RUN_AUTOMATION = AutomationIntents.ACTION_RUN_AUTOMATION;
	
	/**
	 * Adds the component used for running the automation to the existing intent.
	 * 
	 * @param context
	 * @param intent
	 * @return
	 */
	public static void setRunAutomationComponent(Context context, Intent intent) {
		ComponentName component = intent.getComponent();
		String packageName = component.getPackageName();
		
		if (packageName != null) {
			
			PackageManager pm = context.getPackageManager();
			Intent runAutomationIntent = new Intent(ACTION_RUN_AUTOMATION);
			
			List<ResolveInfo> rilist = pm.queryBroadcastReceivers(runAutomationIntent, 0);
			for (ResolveInfo ri : rilist) {
				//Log.i(TAG, "name: " + ri.loadLabel(pm));
				//Log.i(TAG, "Package name: " + ri.activityInfo.packageName);
				//Log.i(TAG, "Package name: " + ri.activityInfo.name);
				//Log.i(TAG, "more " + ri.serviceInfo);
				if (packageName.equals(ri.activityInfo.packageName)) {
					// Let's use this intent
					ComponentName cn = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
					
					intent.putExtra(EXTRA_RUN_AUTOMATION_COMPONENT, (String) cn.flattenToString());
					return;
				}
			}
		}
		
		// No automation target found.
		intent.putExtra(EXTRA_RUN_AUTOMATION_COMPONENT, (String) null);
		return;
		
	}
	
	/**
	 * Clears the internal extra.
	 * 
	 * @param intent
	 */
	public static final void clearInternalExtras(Intent intent) {
		intent.putExtra(EXTRA_RUN_AUTOMATION_COMPONENT, (String) null);
	}
	
	/**
	 * Returns the broadcast intent whose component has been stored previously.
	 * 
	 * @param intent
	 * @return
	 */
	public static final Intent getRunAutomationIntent(Intent intent) {
		Intent runIntent = null;
		
		if (intent.hasExtra(EXTRA_RUN_AUTOMATION_COMPONENT)) {
			runIntent = new Intent(intent);
	
			ComponentName component = ComponentName.unflattenFromString(intent.getStringExtra(EXTRA_RUN_AUTOMATION_COMPONENT));
			runIntent.setAction(ACTION_RUN_AUTOMATION);
			runIntent.setComponent(component);
			runIntent.setData(null);
			clearInternalExtras(runIntent);
			
		}
		return runIntent;
	}
	
	/**
	 * Whether a valid component to run the automation has been set.
	 * 
	 * @param intent
	 * @return
	 */
	public static final boolean isRunAutomationIntent(Intent intent) {
		return intent.hasExtra(EXTRA_RUN_AUTOMATION_COMPONENT);
	}
}
