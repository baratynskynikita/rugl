Credits for the block texture go to rhodox. Go to http://painterlypack.net/
and fling some ducats at him.

Installation:

	1) Install the apk as usual with
			"adb -d install MineDroid.apk"
	or download it from your phone's browser and tap it in the download list to
	install.
	2) Copy your ".minecraft" installation directory over to 
	the root of your phone's storage. Only the saves/World1 directory is actually 
	needed, so you can save some space by not copying the bin, resources and other 
	world directories
	3) That's it. If there's some problem with loading the level.dat in the save 
	directory you'll get a notification when you try to start the app.
	The starting position of the camera is taken from the level.dat file and 
	the nether is not supported, so make sure you save your game in the over-world
	before you copy the save across

Controls:

	Touch anywhere on the screen to create a touchstick - on the left half of 
	the screen to control position, on the right half to control direction.
	Look mode is inverted, because who doesn't enjoy pretending that their head 
	is an aeroplane?
	
	Tapping on the right stick will cause the view frustum to freeze in position.
	You can still move the camera around, but only the chunklets that are visible 
	from where you tapped will be rendered. Tap again to unfreeze the frustum.
	
Getting the source:
	You'll need the eclipse projects at
	http://code.google.com/p/rugl/source/browse/#svn/trunk/droid
	
	For those with an eclipse compiler setup as prissy as mine, the missing 
	javadoc warnings are Google's fault.

What I plan to do next:

	* Play around with interface options. Touch-screen stick emulations suck, how 
		will you jump/crouch, left/right click? How will the inventory and crafting 
		work? As a requirement of this:
	* Toggle between normal walking and noclip flying
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