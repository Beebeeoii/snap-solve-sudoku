# Frequently Asked Questions

- [Frequently Asked Questions](#frequently-asked-questions)
  - [How do I improve the accuracy of detection?](#how-do-i-improve-the-accuracy-of-detection)

## How do I improve the accuracy of detection

Many factors affect the accuracy of detection, especially when it is a digital picture (picture of a computer screen, monitor etc). This is because of the digital noise that stems from interference when the camera is taking a snapshot.

Options to improve accuracy of detection of a **digital picture**:

  1) Before capturing the picture, adjust as much as possible to reduce noise seen in the camera preview
    - This is usually achieved when the board is as big as possible in the camera preview
  2) Screenshot the board and import via the gallery feature. This removes digital noise interference completely.

Options to improve accuracy of detection of a **normal picture**:

  1) Stabilise camera and allow it to auto-focus before snapping
  2) Adjust such that sudoku board fills most of the area in the camera preview

## Difference between V2 and V1

Snap Solve Sudoku V1 utilises Tesseract 3 for digit recognition while V2 uses a custom convolutional neural network machine learning model via Tensorflow and Keras. This enables improvement for recognition to be made easily and on the fly. You can update the model directly from the app whenever there is one available!

Completely rewritten in Kotlin, Snap Solve Sudoku V2 utilises coroutines as much as it is feasible to speed up processes in image processing. Although V1 utilises multithreading as well, coroutines make it much easier and less cumbersome.

UI and UX wise, V2 is more appealing and more modernised.
