package com.kts.safetext;

import android.database.ContentObserver;


class MmsObserver extends ContentObserver {

        public MmsObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
        	// TODO call the filtering method here
        	SafeTextService.getServiceObject().PictureRecieved();
            super.onChange(selfChange);
        }

    }
