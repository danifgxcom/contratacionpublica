// Override Node.js version check
const originalVersions = process.versions;
const patchedVersions = { ...originalVersions, node: '18.13.0' };

// Replace the process.versions object with our patched version
Object.defineProperty(process, 'versions', {
  get: function() {
    return patchedVersions;
  }
});

console.log('[INFO] Node.js version overridden to 18.13.0 for compatibility with Angular CLI');