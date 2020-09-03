# 🆕 Snap Solve Sudoku V2

[![GitHub release](https://img.shields.io/github/release/Beebeeoii/SnapSolveSudoku.svg)](https://github.com/Beebeeoii/SnapSolveSudoku/releases)
[![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)](https://github.com/Beebeeoii/SnapSolveSudoku/graphs/commit-activity)
[![GitHub issues](https://img.shields.io/github/issues/Beebeeoii/SnapSolveSudoku.svg)](https://GitHub.com/Beebeeoii/SnapSolveSudoku/issues/)
[![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/Beebeeoii/SnapSolveSudoku/blob/v2/LICENSE)

Snap Solve Sudoku V2 is completely rewritten from V1. Leveraging on Machine Learning using Tensorflow and personalised image processing algorithms with the use of OpenCV, Snap Solve Sudoku aims to be accurate in detecting and recognising sudoku boards and its digits. Regardless of image source, be it from desktop monitors or printed media, the app's accuracy has been overwhelming!

[👉 Try it from Google Play now! 👈](https://play.google.com/store/apps/details?id=com.beebeeoii.snapsolvesudoku)

## Screenshots

| ![Main Screen](../v2/screenshots/main_screen.jpg) | ![Import](../v2/screenshots/import.jpg) |
|:-------------------:|:--------------:|
| Main Screen | Import from Camera or Gallery |

| ![Camera](../v2/screenshots/camera.jpg) | ![Details](../v2/screenshots/details.jpg) | ![History](../v2/screenshots/history.jpg) | ![Settings](../v2/screenshots/settings.jpg) |
|:--------------:|:---------------:|:---------------:|:----------------:|
| Camera         | Details         | History         | Settings         |

## Features

- Smart OCR sudoku board detection and recognition
- Solve for all available solutions
- Awesome sudoku board UX
- Save solved entries into history for future review
- Machine learning model in place for improvement in future digit recognition
- Share your board with friends with just a click

## How to use

### Using camera

1️⃣ Grant permissions for storage and camera access

2️⃣ Scan sudoku board with in-app camera

3️⃣ Ensure pink outline is drawn around the correct sudoku board

4️⃣ Snap!

### Importing from gallery

1️⃣ Grant permissions for storage and camera access

2️⃣ Import sudoku board picture from gallery

3️⃣ Crop the borders of the sudoku board

4️⃣ Submit!

## FAQ

Kindly refer to the [FAQ section](FAQ.md) for the questions you may have!

## Contributing

Looking for contributors to work on image processing algorithm, especially for images of monitors/screens. Much difficulties are met in dealing with the digital noise (Moire Patterns) evident in certain smartphone cameras.

Please refer to CONTRIBUTING.md for more details.

## Upcoming Features

Planned features can be found under the [upcoming features section](UPCOMING_FEATURES.md)!

## License

    MIT License

    Copyright (c) 2020 Beebeeoii

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
