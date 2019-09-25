# Deviceprovider Communication Demo

Simple demo of the communication between Axessions and DeviceProvider.

## Getting Started
1. Login as a user with administration privileges and create a device provider.
2. Download the device provider configuration.
3. Name it axs.json and place it in src/main/resources/
4. To protect the vault, an environment variable must be provided called VAULT_PASS. 

5. Setup your IDE. Using Intellij, you have to import as a gradle project mark src/main/java as the sources root and
the newly created src/main/resources as the resources root and set the JDK to use for the project to version 11.

6. Build the project, on MacOs run
```
$ ./gradlew build
```
7. Run the project

## Using the Demo

Create devices on the website and set the device provider value to the name of your device provider.
Update your devices and trigger actuators. At the time of this writing, an actuator is added by updating any field of the device the device. 

### Disclaimer
These instructions may become outdated as the project develops. Contact us if you get stuck.

Written: 25/9/2019