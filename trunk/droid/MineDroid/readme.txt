Credits for the block texture go to rhodox. Go to http://painterlypack.net/
and fling some ducats at him.

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

	Touch anywhere on the screen to create a touchstick - on the left half of 
	the screen to control position, on the lower-right half to control direction.
	
	Tap at the top-right half of the screen to jump, long-press in the same area to 
	start crouching, tap again to stop. Note that crouching is only cosmetic for 
	now- you will still fall off blocks and move at full speed.
	
	The menu button will give an extensive tree of configuration options.
	
Glitchy controls?

	If the touchstick controls are acting all glitchy and rubbish, you might be 
	falling foul of the problem illustrated in this video:
	
	http://www.youtube.com/watch?v=hVlsRCMltDg
	
	It seems that some phones have screens that are good enough for pinch-to-zoom
	style multitouch gestures, but can't actually track two touches reliably.
	This is a hardware limitation, and there's not a lot that can be done to
	rectify it in software. I can only suggest you place your thumbs in opposite 
	corners of the screen - this should minimise any opportunities for the touches
	to cross axes and the resulting glitchyness to manifest.
	
Getting the source:

	The source is organised in three Eclipse projects at
	
	http://code.google.com/p/rugl/source/browse/#svn/trunk/droid
	
	You'll need the Configuration, DroidRUGL and MineDroid projects.
	For those with an Eclipse compiler setup as prissy as mine, the missing 
	javadoc warnings are Google's fault.
	
	If you want to work on the OpenGLES 1.0 version, switch the MineDroid project 
	to the branch at
	
	http://code.google.com/p/rugl/source/browse/#svn/trunk/droid/gles10

What I plan to do next:

	* Play around with interface options. Touch-screen stick emulations suck, how 
		will you left/right click? How will the inventory and crafting work?
	As a requirement of this:
	* Placing and breaking blocks
	
Stuff that should be done that I can't do

	* Try this on loads of different phones, find and squash the inevitable bugs,
		work around the performance oddities, get exasperated at broken library
		implementations

Stuff that should be done that I'm not in a huge rush to do:

	* More block types
	* Odd-shaped blocks - half block, steps, liquids
	* Play around with render distance and fog parameters - need to see as far as 
		possible into generated chunklets while still hiding ungenerated. 
		Maybe make it dynamic?
	* Decrease size of chunklets - tradeoff between more culled geometry and 
		greater rendering overhead
	* Proper occlusion culling, block or chunklet-based - it'll be complex and 
		I'm not sure it'll prove worthwhile.
	* Store chunks in a proper spatial datastructure
 	
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