/*   
 * 	 Copyright (C) 2008-2009 pjv (and others, see About dialog)
 * 
 * 	 This file is part of OI About.
 *
 *   OI About is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OI About is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OI About.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   
 *   
 *   Based on veecheck-sample.
 */

package org.openintents.about.versioncheck;

import android.content.Context;

import com.tomgibara.android.veecheck.VeecheckReceiver;
import com.tomgibara.android.veecheck.VeecheckSettings;
import com.tomgibara.android.veecheck.VeecheckState;
import com.tomgibara.android.veecheck.util.PrefSettings;
import com.tomgibara.android.veecheck.util.PrefState;

/**
 * @author pjv
 *
 */
public class VersioncheckBroadcastReceiver extends VeecheckReceiver {

	@Override
	protected VeecheckSettings createSettings(Context context) {
		return new PrefSettings(context);
	}
	
	@Override
	protected VeecheckState createState(Context context) {
		return new PrefState(context);
	}
	
}
