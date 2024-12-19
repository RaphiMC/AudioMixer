# AudioMixer
High performance Java audio mixing library.

## Features
- Realtime audio mixing to a playback device
- Rendering mixed audio to a file
- Sampled audio playback
  - Pitch control
  - Streaming (Pull and Push support)
    - Pull: Audio data is read from an InputStream
    - Push: Audio data is enqueued by the application
- Basic 3D spatial audio
  - Position and orientation control
  - Distance attenuation
- Audio effects
  - Volume control
  - Stereo panning control
  - Bass Boost
  - Bit Crusher
- Modulation effects
  - Pitch modulation (Vibrato)
  - Volume modulation (Tremolo)
- Oscillators
  - Sine, Square, Triangle, Sawtooth
  - Random noise
- High performance (Mixes thousands of simultaneously playing sounds in realtime)

## Releases
### Gradle/Maven
To use AudioMixer with Gradle/Maven you can get it from [Maven Central](https://mvnrepository.com/artifact/net.raphimc/audio-mixer), [Lenni0451's Maven](https://maven.lenni0451.net/#/releases/net/raphimc/audio-mixer) or [Jitpack](https://jitpack.io/#RaphiMC/AudioMixer).
You can also find instructions how to implement it into your build script there.

### Jar File
If you just want the latest jar file you can download it from [GitHub Actions](https://github.com/RaphiMC/AudioMixer/actions/workflows/build.yml) or [Lenni0451's Jenkins](https://build.lenni0451.net/job/AudioMixer/).

## Usage
AudioMixer provides multiple ``AudioMixer`` implementations to choose from.
* ``BackgroundSourceDataLineAudioMixer``: Easiest to use implementation for realtime audio mixing. It mixes audio in the background and sends it to a SourceDataLine.
* ``SourceDataLineAudioMixer``: Similar to ``BackgroundSourceDataLineAudioMixer`` but you have to call ``mixSlice`` manually. This allows you to mix audio in sync with the rest of your application.
* ``AudioMixer``: Base mixer class. Intended for rendering audio to a file.

After creating an ``AudioMixer`` instance you can play ``Sound`` instances with it using the ``playSound`` method.

## Examples
Examples can be found in the [src/test](/src/test) directory.

## Contact
If you encounter any issues, please report them on the
[issue tracker](https://github.com/RaphiMC/AudioMixer/issues).  
If you just want to talk or need help implementing AudioMixer feel free to join my
[Discord](https://discord.gg/dCzT9XHEWu).
