/**
 *  Blink Light on Arrival
 *
 *  Copyright 2015 Ethan Levine
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Blink Light on Arrival",
    namespace: "ethanlevine",
    author: "Ethan Levine",
    description: "Blink light in specified color when someone arrives home",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

//Hue color picker prefs and setter code borrowed from Hue Mood Lighting SmartApp

preferences {
	section("Settings") {
		// TODO: put inputs here
        input "thePerson", "capability.presenceSensor", title: "Who?", required: true, multiple: false
        input "theHue", "capability.switch", title: "Which Hue Bulbs?", required:true, multiple:false
        input "varBlinkCount", "number", required: true, title: "Number of blinks"
		input "varDelayTime", "number", required: true, title: "How long should the light be on when it blinks?"
        input "color", "enum", title: "Hue Color?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
		input "theButton", "capability.switch", title: "button" //This is for testing purposes only
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	state.count = 0
    state.blinkPrefs = 0
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(thePerson, "presence.present", presenceDetectedHandler)
    subscribe(theButton, "switch.on", presenceDetectedHandler) //This is for testing purposes only
}

// TODO: implement event handlers
def presenceDetectedHandler(evt) {
	log.debug("presenceDetectedHandler called: $evt")
    state.count = 0
    log.debug("Resetting state counter to 0")
    state.blinkPrefs = varBlinkCounter
    log.debug("Setting state.blinkPrefs to varBlinkCounter")
    
    try {
        //Get current state of Hue bulb
        state.oldSwitch		= theHue.currentValue("switch")		//on or off
        state.oldHue		= theHue.currentValue("hue")		//color
        state.oldSaturation	= theHue.currentValue("saturation")	//color accuracy
        state.oldLevel		= theHue.currentValue("level")		//brightness

        log.debug("Previous Hue Bulb Properties:")
        log.debug("Old State: $oldSwitch")
        log.debug("Old Hue: $oldHue")
        log.debug("Old Saturation: $oldSaturation")
        log.debug("Old Level: $oldLevel")

        //Turn bulb off
        theHue.off()
        log.debug("Turning Hue off to prep new color settings")

        //Set bulb color settings from prefs
        def hueColor = 0
        def saturation = 100
        def lightLevel = 100

		//Match color picked in Prefs
        switch(color) {
            case "White":
                hueColor = 52
                saturation = 19
                break;
            case "Daylight":
                hueColor = 53
                saturation = 91
                break;
            case "Soft White":
                hueColor = 23
                saturation = 56
                break;
            case "Warm White":
                hueColor = 20
                saturation = 80 //83
                break;
            case "Blue":
                hueColor = 70
                break;
            case "Green":
                hueColor = 39
                break;
            case "Yellow":
                hueColor = 25
                break;
            case "Orange":
                hueColor = 10
                break;
            case "Purple":
                hueColor = 75
                break;
            case "Pink":
                hueColor = 83
                break;
            case "Red":
                hueColor = 100
                break;
        }

		//Collect Hue values for setting bulb
        def newValue = [hue: hueColor, saturation: saturation, level: lightLevel as Integer ?: 100]
        log.debug "Setting bulb to new values: = $newValue"

		//Set the bulb
        theHue.setColor(newValue)
        log.debug("New values have been set")

/*
        //Loop x times
        for (int i = 0; i < varBlinkCount; i++) {
            log.debug("looping through blink counter: iteration #${i+1}")
            //Turn bulb on
            theHue.on()
            log.debug("Turning Hue ON.")
            //wait x ms and then turn bulb off
            //runIn(10000, turnOff)
            theHue.off()
            log.debug("Turning Hue OFF.")
        }
*/        
        //This is for testing purposes only
       	log.debug("Will try to blink light after one second of darkness")
        runIn(1, tryBlinkLight)
            
        
    } catch (all) {
		log.debug("\n--------\n\nWHOOPS!\n${all}\n--------")
    }
}

//Used to turn off the bulb via runIN()
def turnOff() {
	theHue.off()
    log.debug("Turning Hue OFF.")
}

//Everything past here is NEW

//def blinkCounter = 0 //Replacing with state.count

def tryBlinkLight() {
	try {
    	log.debug("Trying to blink light. Checking blinkcounter")
        if (state.count < varBlinkCount) {
        	log.debug("Blink Counter is < prefs setting. Count: ${state.count}, Pref: ${varBlinkCount}")
            log.debug("Turning light ON")
            lightOn()
            log.debug("Turning light OFF after ${varDelayTime} seconds of illumination")
            runIn(varDelayTime, lightOff)
        } else {
        	log.debug("blinkCounter _${state.count}_ is >= varBlinkCounter _${state.count}_. Light will not blink again")
            //Call reset function to return lights to previous state
            resetLightState()    
        }
    } catch (all) {
    	log.debug("\n--------\n\nWHOOPS! -- blinkLight\n${all}\n--------")
    }

}

def lightOn() {
	theHue.on()
    log.debug("The light is ON")
}

def lightOff() {
	theHue.off()
    log.debug("The light is OFF")
    log.debug("Incrementing blinkCounter")
    state.count = state.count + 1
    log.debug("blinkCounter is now = ${state.count}")
    log.debug("Trying to blink again after one second")
    runIn(1, tryBlinkLight)
}

def resetLightState() {
	//Recall old bulb settings from before SmartApp ran
        def oldValue = [hue: state.oldHue, saturation: state.oldSaturation, level: state.oldLevel as Integer]
        log.debug "Setting bulb to old values: = $oldValue"

		//Reset bulb to previous color/state settings
        theHue.setColor(oldValue)

		//Collect current bulb settings after applying original settings again
        oldHue			= theHue.currentValue("hue")		//color
        oldSaturation	= theHue.currentValue("saturation")	//color accuracy
        oldLevel		= theHue.currentValue("level")		//brightness

        log.debug("Final Hue Bulb Properties:")
        log.debug("Final State: $state.oldSwitch")
        log.debug("Final Hue: $state.oldHue")
        log.debug("Final Saturation: $state.oldSaturation")
        log.debug("Final Level: $state.oldLevel")

		//Return bulb to on/off state it was in when we started
        if (state.oldSwitch == "on") {
           theHue.on()
           log.debug("Hue was previously on. Turning back on.")
        } else {
            theHue.off()
            log.debug("Hue was previously off. Turning back off.")
        }
}