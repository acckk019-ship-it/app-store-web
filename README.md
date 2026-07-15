# Sleek App Store Web Portal

A premium, modern web application catalog built with vanilla HTML, CSS (Roboto & Google Sans fonts), and JavaScript. It supports fully responsive grid views, search filtering, category sorting, detail pages, version history, and file downloads.

## Features

- **Responsive Grid**: Adaptive layout displaying app cards with ratings, download counts, and category tags.
- **Search & Filter**: Search apps by name, description, or category dynamically.
- **Admin Upload Portal**: Easily upload new apps, set developers, versions, file sizes, screenshots, banners, and app binaries.
- **Version History**: Keep track of previous versions, change logs, and download links.
- **Local Helper & GitHub Sync**: A local PowerShell server serves the application locally, accepts uploads, saves files directly to the local folder, and pushes updates to GitHub Pages automatically.
- **Passcode Protection**: Modifying operations (uploading apps, adding new versions) are protected by a customizable passcode.

---

## Local Administration (Uploading Apps)

To manage the app store (uploading new apps, adding screenshots, banners, or new versions) and sync them automatically to your live GitHub website:

1. **Launch the Admin Helper**:
   - Double-click the **StartAdmin** shortcut on your Desktop, or run `StartAdmin.bat` in the project directory.
   - This starts a local background server on `http://localhost:8080` and opens the App Store in your browser.

2. **Access Admin Actions**:
   - When running on `localhost`, you will see a badge in the top bar confirming **Local Admin Mode Active**.
   - Click **Upload app** or **+ Add new version** on an app page.

3. **Authenticate & Publish**:
   - Enter your admin passcode (default: **`1234`**).
   - Enter the app details and upload files (images, zip packages).
   - Click **Publish app**. The helper will automatically save your files locally, update `apps.json`, commit to Git, and run `git push` to redeploy the site on GitHub Pages.

### Changing the Passcode

Open [config.json](config.json) in any text editor and edit the `"passcode"` value:
```json
{
  "passcode": "your_new_passcode"
}
```
Restart the admin helper to apply the new passcode.

---

## Hosting on GitHub Pages

1. Create a new repository on your GitHub account (e.g., `app-store-web`).
2. Push this local directory to your repository:
   ```bash
   git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
   git branch -M main
   git push -u origin main
   ```
3. Enable GitHub Pages:
   - Go to your repository **Settings** -> **Pages**.
   - Under **Build and deployment**, set the source to **Deploy from a branch**.
   - Select the `main` branch and `/ (root)` folder, then click **Save**.
4. The site will be live at `https://YOUR_USERNAME.github.io/YOUR_REPO_NAME/`.
