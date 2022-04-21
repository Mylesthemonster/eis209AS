import pyaudio
import time
import wave
import os.path
import matplotlib.pyplot as plt
import numpy as np

from scipy.io import wavfile as wav
from scipy.fftpack import fft


def uniquify(path):
    filename, extension = os.path.splitext(path)
    counter = 1

    while os.path.exists(path):
        path = filename + "_" + str(counter) + extension
        counter += 1

    return path

CHUNK = 1024  # How many audio saamples per frame
FORMAT = pyaudio.paInt16 # Bytes per sample
CHANNELS = 1 
RATE = 44100 # Samples per second 44.1 khz most common 
RECORD_SECONDS = 5
WAVE_OUTPUT_FILENAME = uniquify("test.wav")

def plt_signal():
   
    raw = wave.open(WAVE_OUTPUT_FILENAME) 
    
    signal = raw.readframes(-1) # reads all the frames, -1 indicates all or max frames
    signal = np.frombuffer(signal, dtype ="int16")
    
    f_rate = raw.getframerate() # gets the frame rate
 
    # to Plot the x-axis in seconds, you need get the frame rate and divide by size of your signal to create a Time Vector spaced linearly with the size of the audio file
    time = np.linspace(
        0, # start
        len(signal) / f_rate,
        num = len(signal)
    )
 
    plt.figure(1)  # using matplotlib to plot, this creates a new figure
     
    plt.title(f'Sound Wave of {WAVE_OUTPUT_FILENAME}')
    plt.xlabel("Time")
    plt.ylabel("Amplitude")
    plt.plot(time, signal)
    plt.show()

    # plt.savefig('filename') # save the plot

def plt_signal_fft():
    
    rate, data = wav.read(WAVE_OUTPUT_FILENAME)
    fft_out = fft(data)
    
    plt.figure(2)
    
    plt.title(f'FFT of {WAVE_OUTPUT_FILENAME}')
    plt.xlabel("Time")
    plt.ylabel("Frequency")
    plt.plot(data, np.abs(fft_out))
    plt.show()
    
print("Prepare to Record")
for i in range(3,0,-1):
    print(f'{i}\n')
    time.sleep(1)
    
print('GO')

p = pyaudio.PyAudio()

frames = []

stream = p.open(format = FORMAT,
                channels = CHANNELS, 
                rate = RATE, 
                input = True,
                frames_per_buffer = CHUNK)

print('****Recording****')

for i in range(0, int(RATE / CHUNK * RECORD_SECONDS)):
    data = stream.read(CHUNK)
    frames.append(data)
    
print('*****Finished****')

stream.stop_stream()
stream.close()
p.terminate()

wf = wave.open(WAVE_OUTPUT_FILENAME, 'wb')
wf.setnchannels(CHANNELS)
wf.setsampwidth(p.get_sample_size(FORMAT))
wf.setframerate(RATE)
wf.writeframes(b''.join(frames))
wf.close()

plt_signal()
plt_signal_fft()
