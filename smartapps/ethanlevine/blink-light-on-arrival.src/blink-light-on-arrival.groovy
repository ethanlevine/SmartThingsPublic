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


preferences {
	section("Settings") {
		// TODO: put inputs here
        input "thePerson", "capability.presenceSensor", title: "Who?", required: true, multiple: false
        input "theHue", "capability.switch", title: "Which Hue Bulbs?", required:true, multiple:false
        input "varBlinkCount", "number", required: true, title: "Number of blinks"
		input "varDelayTime", "number", required: true, title: "Delay between blinks"
        input "color", "enum", title: "Hue Color?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

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
}

// TODO: implement event handlers
def presenceDetectedHandler(evt) {
	log.debug("presenceDetectedHandler called: $evt")
    try {
        //Get current state of Hue bulb
        def oldSwitch		= theHue.currentValue("switch")		//on or off
        def oldHue			= theHue.currentValue("hue")		//color
        def oldSaturation	= theHue.currentValue("saturation")	//color accuracy
        def oldLevel		= theHue.currentValue("level")		//brightness

        log.debug("Previous Hue Bulb Properties:")
        log.debug("Old State: $oldSwitch")
        log.debug("Old Hue: $oldHue")
        log.debug("Old Saturation: $oldSaturation")
        log.debug("Old Level: $oldLevel")

        //log.debug("current values = ${huePreviousState}")

        //Turn bulb off
        theHue.off()

        //Set bulb color settings from prefs
        def hueColor = 0
        def saturation = 100
        def lightLevel = 100

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

        def newValue = [hue: hueColor, saturation: saturation, level: lightLevel as Integer ?: 100]
        log.debug "Setting bulb to new values: = $newValue"

        theHue.setColor(newValue)

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
            
        //Reset bulb to previous color/state settings
        def oldValue = [hue: oldHue, saturation: oldSaturation, level: oldLevel as Integer]
        log.debug "Setting bulb to old values: = $oldValue"

        theHue.setColor(oldValue)

        oldHue			= theHue.currentValue("hue")		//color
        oldSaturation	= theHue.currentValue("saturation")	//color accuracy
        oldLevel		= theHue.currentValue("level")		//brightness

        log.debug("Final Hue Bulb Properties:")
        log.debug("Final State: $oldSwitch")
        log.debug("Final Hue: $oldHue")
        log.debug("Final Saturation: $oldSaturation")
        log.debug("Final Level: $oldLevel")

        if (oldSwitch == "on") {
           theHue.on()
           log.debug("Hue was previously on. Turning back on.")
        } else {
            theHue.off()
            log.debug("Hue was previously off. Turning back off.")
        }
    } catch (all) {
		log.debug("\n--------\n\nWHOOPS!\n${all}\n--------")
    }
}

def turnOff() {
	theHue.off()
    log.debug("Turning Hue OFF.")
}