package com.example.acer.wifidirecthostservice;


import android.app.Application;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/* @project 
 * 
 * License to access, copy or distribute this file.
 * This file or any portions of it, is Copyright (C) 2012, Radu Motisan ,  http://www.pocketmagic.net . All rights reserved.
 * @author Radu Motisan, radu.motisan@gmail.com
 * 
 * This file is protected by copyright law and international treaties. Unauthorized access, reproduction 
 * or distribution of this file or any portions of it may result in severe civil and criminal penalties.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * @purpose 
 * Cursor Overlay Sample
 * (C) 2012 Radu Motisan , all rights reserved.
 */


public class Singleton extends Application {
	private static 		Singleton				m_Instance;
	 // screen metrics
	private				float					m_fDensity 					= 0;
	private				int						m_nScreenW 					= 0,
												m_nScreenH 					= 0;					
	
	//public 				GUI						m_guiInst					= null;

	public				CursorService			m_CurService				= null;
    public static Bus bus = new Bus();
	
	/*---------------------------------------------------------------------------------------------
	 * Singleton Init instance
	 *--------------------------------------------------------------------------------------------*/
	public Singleton() {
		super();
		m_Instance = this;
	}
	// Double-checked singleton fetching
	public static Singleton getInstance() {
		if(m_Instance == null) {
			synchronized(Singleton.class) {
				if(m_Instance == null) new Singleton();
			}
		}
		return m_Instance;
	}
	@Override
	public void onCreate()
	{
		super.onCreate();
	}
	
	
}

