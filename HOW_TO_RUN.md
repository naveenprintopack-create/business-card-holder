# Card Holder — Setup Guide (no coding needed)

> **Don't have Android Studio / a laptop that can run it?**
> Skip this file and open **NO_INSTALL_SETUP.md** instead — it builds the
> app for you in the cloud, using only a web browser.


This is a complete Android app: scan a business card with your camera, it
reads the text automatically (fully offline, on your phone — nothing is
uploaded anywhere), you review/correct the details, and it's saved locally
in a searchable list.

You don't need to write or understand any code. Just follow these steps.

## Step 1 — Install Android Studio
1. Go to https://developer.android.com/studio and download Android Studio
   for Windows/Mac/Linux (whichever you use).
2. Run the installer, keep all default options, let it finish downloading
   the Android SDK (this can take 10–20 minutes on first run).

## Step 2 — Open this project
1. Unzip the `BusinessCardHolder.zip` file I gave you, anywhere on your
   computer (e.g. Desktop).
2. Open Android Studio → **File > Open** → select the unzipped
   `BusinessCardHolder` folder → OK.
3. Android Studio will show "Gradle Sync" in the bottom status bar. Wait
   for it to finish (first time can take several minutes — it's
   downloading the pieces the app needs). If it asks to update the Gradle
   version or "Agree" to anything, click **OK / Accept**.

## Step 3 — Connect your Android phone
1. On your phone: go to **Settings > About phone** and tap **Build number**
   7 times until it says "You are now a developer".
2. Go to **Settings > Developer options** and turn on **USB debugging**.
3. Connect your phone to your computer with a USB cable.
4. Your phone will show a popup "Allow USB debugging?" — tap **Allow**.

## Step 4 — Run the app
1. In Android Studio, at the top you'll see a dropdown that should show
   your phone's name, and a green ▶️ **Run** button next to it.
2. Click the green ▶️ **Run** button.
3. Android Studio will build the app and install it on your phone
   automatically. First build can take a few minutes.
4. The app will open on your phone. Grant the **Camera permission** when
   asked.

That's it — you now have the app installed and running locally on your
phone. Every time you make no changes and just want to reopen it, you'll
find it on your phone's app drawer as **"Card Holder"**.

## Using the app
- Tap the **+** button → point camera at a business card → **Capture**.
- The app reads the text and pre-fills Name, Company, Phone, Email,
  Website, Address as best it can guess.
- Check/correct any fields, then tap **Save Card**.
- Back on the main screen, use the search box to find a card by name,
  company, phone, or email.
- Tap any card to view/edit its details, call the contact directly, or
  delete it.

## If something goes wrong when building
Copy the exact red error text shown in Android Studio's bottom panel
("Build" tab) and send it to me — I'll fix the code for you. Common
first-time issues are just Android Studio still downloading components,
which usually resolves itself if you wait and click **Sync Project with
Gradle Files** (the elephant/refresh icon in the toolbar).

## Notes on privacy
Everything — photos, extracted text, and details — is stored only in this
app's private storage on your phone (using a local database called Room).
Nothing is sent to the internet. Uninstalling the app deletes all saved
cards, so don't uninstall it if you want to keep your data (a future
version could add an export/backup feature if you'd like that).
