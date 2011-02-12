Thanks to:
	Credits for the block texture go to rhodox. Go to http://painterlypack.net/
	and fling some ducats at him.
	Richard Invent for the help debugging on OpenGLES1.0 devices

Installation:

	1) Install the apk as usual with
			"adb -d install MineDroid.apk"
	or download it from your phone's browser and tap it in the download list to
	install.
	3) Run the app, a ".minecraft/saves" directory will be created on your 
	phone's storage
	2) Copy your "World1" directory over into the saves directory on your phone.
	3) That's it. If there's some problem with loading the level.dat in the save 
	directory you'll get a notification when you try to start the app.
	The starting position of the camera is taken from the level.dat file and 
	the nether is not supported, so make sure you save your game in the over-world
	before you copy the save across

Controls:

	The squares in the corners are thumbstick, left for movement, right for steering
	Tap or swipe upwards on the rectangle above the right thumbstick to jump,
	long-press or swipe downwards to crouch.
	
	Tap on the hot bar to select a tool.
	Tap either thumbstick to use the held item at the center of the screen, tap 
	on the screen to target elsewhere.
	Appropriate tool use (e.g.: a shovel on dirt or a pick on stone) is made easy:
	a single tap initiates the block-breaking, which will continue as long as the
	player is in range of the target block. Inappropriate tool use is made difficult:
	the touch must be held on target until it is broken.
	
	Blocks can be dragged from the hot bar and placed immediately, without
	having to select it as a held item
	
	The menu button will give an extensive tree of configuration options.
	Save a configuration with the name "default" (note all lower-case), and it'll
	be applied automatically at startup
	
Glitchy controls?

	If the touchstick controls are acting all glitchy and rubbish, you might be 
	falling foul of the problem illustrated in this video:
	
	http://www.youtube.com/watch?v=hVlsRCMltDg
	
	It seems that some phones have screens that are good enough for pinch-to-zoom
	style multitouch gestures, but can't actually track two touches reliably.
	This is a hardware limitation, and there's not a lot that can be done to
	rectify it in software. In the settings menus, to
	
	/BlockView/Interface/Left stick/Pad area
	
	The default setting is "0.0, 0.0, 150.0, 150.0" change the second number to
	330, i.e.: so that it reads "0.0, 330.0, 150.0, 150.0". This'll put the left 
	thumbstick in the upper-left corner. Remember to save the settings with the
	name "default".
	
Getting the source:

	The source is organised in three Eclipse projects at
	
	http://code.google.com/p/rugl/source/browse/#svn/trunk/droid
	
	You'll need the DroidRUGL and MineDroid projects from that repository, 
	and also the Preflect project from
	
	http://code.google.com/p/preflect
	
	For those with an Eclipse compiler setup as prissy as mine, the missing 
	javadoc warnings are Google's fault.
	
Getting the source, step-by-step:

	1) Download and install "Eclipse IDE for Java Developers" from
		http://www.eclipse.org/downloads/
	2) Open Eclipse and create a new workspace
	3) Install the subclipse SVN plugin for eclipse. Instructions at
		http://subclipse.tigris.org/servlets/ProjectProcess?pageID=p4wYuA
	4) Install the Android development tools and ADT eclipse plugin.
		http://developer.android.com/sdk/installing.html
	5) Add the RUGL repository to subclipse:
		In Eclipse, "Window" menu - "Open Perspective" - "Other"
		Choose "SVN Repository Exploring"
		Right-click in the currently-blank "SVN Repositories" tab. "New" - 
			"Repository Location..."
		The URL is "http://rugl.googlecode.com/svn"
	6) Similarly, check out the library at 
			"http://preflect.googlecode.com/svn/trunk"
	6) Check out the projects:
		Click on the newly-added repository entries to open them
		Click on "trunk" then "droid" when they appear
		Right-click on "DroidRUGL", choose "Checkout..."
		You don't have to change anything in the dialog, just hit "Finish"
		Do the same thing to check out "Preflect" and "MineDroid"
	7) You've now got local copies of the code - huzzah!
			
Stuff that should be done that I can't do by myself

	* Try this on loads of different phones, find and squash the inevitable bugs,
		work around the performance oddities, get exasperated at broken library
		implementations

Stuff that should be done that I'm not in a huge rush to do:

	* More block types
	* Odd-shaped blocks - steps, liquids
	* Play around with render distance and fog parameters - need to see as far as 
		possible into generated chunklets while still hiding ungenerated. 
		Maybe make it dynamic?
	* Decrease size of chunklets - tradeoff between more culled geometry and 
		greater rendering overhead
	* Proper occlusion culling, block or chunklet-based - it'll be complex and 
		I'm not sure it'll prove worthwhile.
	
Stuff that I'm not going to do, and that I don't think should be done:

	* Minecraft game stuff, to whit:
	* terrain generation
	* mobs
	* chunk updates - liquid flow, sand/gravel motion, fire, lighting, etc
	* items, inventory, crafting
	* etc
	
What's the point?

	My hope is that MineDroid can reduce the development risk for an official 
	Mojang-made android port by demonstrating:
	* that performance is good enough
	* that touch-screen interfaces work well enough
	* that device fragmentation is manageable
	Failing that, it'll be easier to make an android port once Minecraft is 
	open-sourced.