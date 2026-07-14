# Card Holder — Zero-Install Setup (browser only)

You will not install Android Studio or anything code-related. You'll use a
free website (GitHub) that builds the app for you on its own servers, and
you just download the finished file and put it on your phone.

Everything here is done with mouse clicks in a web browser.

---

## Step 1 — Unzip the project on your computer
1. Find the `BusinessCardHolder.zip` file I gave you.
2. Right-click it → **Extract All** (Windows) or double-click it (Mac) →
   you'll get a folder called `BusinessCardHolder`.

(You're just extracting a zip file here — no coding tool involved.)

## Step 2 — Create a free GitHub account
1. Go to https://github.com/signup
2. Enter your email, create a password, pick a username → follow the
   prompts to verify your email.

## Step 3 — Create a new repository (a project folder on GitHub)
1. Once logged in, click the **+** icon (top right) → **New repository**.
2. Repository name: `business-card-holder`
3. Leave everything else as default → click **Create repository**.

## Step 4 — Upload your project files
1. On the new (empty) repository page, click the link that says
   **"uploading an existing file"**.
2. On your computer, open the `BusinessCardHolder` folder (from Step 1).
3. Select **everything inside it** (the `app` folder, `build.gradle`,
   `settings.gradle`, `gradle.properties`, `HOW_TO_RUN.md`, this file) —
   do **not** upload the outer folder itself, go inside it and select its
   contents.
   - **Important:** don't worry about a folder called `.github` — you
     may or may not see it depending on your computer. Skip it for now;
     we'll add it in the next step instead.
4. Drag all the selected files/folders into the browser upload box.
   Wait for the upload progress to finish.
5. Scroll down, click the green **Commit changes** button.

## Step 5 — Add the auto-build instructions
This is the one piece we'll create directly on GitHub's website instead of
uploading, so it works the same on every computer:

1. In your repository, click **Add file** (top right area) → **Create new
   file**.
2. In the file name box, type exactly:
   `.github/workflows/build.yml`
   (typing the slashes makes GitHub create those folders automatically).
3. Paste this into the big text box below it:

```yaml
name: Build APK

on:
  push:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: 8.6

      - name: Build debug APK
        run: gradle assembleDebug --no-daemon

      - name: Upload APK as downloadable artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-debug-apk
          path: app/build/outputs/apk/debug/*.apk
```

4. Scroll down → click **Commit changes**.

## Step 6 — Let GitHub build your app
1. Click the **Actions** tab at the top of your repository.
2. You'll see a build running (yellow dot = in progress). Click into it.
3. Wait 3–8 minutes. A green checkmark ✅ means it succeeded.
   - A red ❌ means something failed — click into the failed step, copy
     the red error text, and send it to me. I'll fix the project files.

## Step 7 — Download the app file (APK)
1. Still on that build's page, scroll down to **Artifacts**.
2. Click **app-debug-apk** to download it — it downloads as a `.zip`.
3. Unzip it → you'll find a file named something like `app-debug.apk`.

## Step 8 — Install it on your phone
1. Get `app-debug.apk` onto your phone — easiest way: email it to
   yourself, or upload it to Google Drive and open Drive on your phone,
   or use a USB cable to copy it over.
2. On your phone, tap the `.apk` file to install it.
3. Your phone will warn about "unknown sources" the first time — tap
   **Settings**, allow installs from that app (e.g. Gmail/Drive/Files),
   then go back and tap the file again → **Install**.
4. Open the app, grant Camera permission when asked. Done!

---

## After this first time
Any time you want to update the app later, you'd just re-upload changed
files on GitHub and repeat Steps 6–8 — no reinstalling anything on your
computer.

## If you get stuck
Send me a screenshot or the exact error text at whichever step you're
stuck on (uploading, the Actions build, or installing on your phone), and
I'll walk you through fixing it.
