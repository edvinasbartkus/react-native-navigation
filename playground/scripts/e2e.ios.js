const _ = require('lodash');
const shellUtils = require('shell-utils');

const release = _.includes(process.argv, 'release');

function buildProjForDetox() {
  const scheme = release ? `playground_release` : `playground`;

  shellUtils.exec.execSync(`echo 'travis_fold:start:xcodebuild'`);
  const cmd = `RCT_NO_LAUNCH_PACKAGER=true
          cd ios && xcodebuild
            -scheme ${scheme}
            ${release ? 'clean build' : 'build'}
            -project playground.xcodeproj
            -sdk iphonesimulator
            -derivedDataPath ./DerivedData/playground`;

  if (isInstalled(`xcpretty`)) {
    shellUtils.exec.execSync(`${cmd} | xcpretty && exit \${PIPESTATUS[0]}`);
  } else {
    shellUtils.exec.execSync(`${cmd}`);
  }
  shellUtils.exec.execSync(`echo 'travis_fold:end:xcodebuild'`);
}
function isInstalled(what) {
  try {
    return shellUtils.exec.execSyncRead(`which ${what}`);
  } catch (e) {
    return false;
  }
}

function e2e() { //eslint-disable-line
  try {
    shellUtils.exec.execSync(`echo 'travis_fold:start:detox-ios'`);
    shellUtils.exec.execSyncSilent(`watchman watch-del-all || true`);
    const detoxAppBuildPath = `ios/DerivedData/playground/Build/Products/${release ? 'Release' : 'Debug'}-iphonesimulator/playground.app`;

    shellUtils.exec.execSync(`detoxAppBuildPath="${detoxAppBuildPath}"
                              BABEL_ENV=test
                              ./node_modules/mocha/bin/mocha e2e
                                --timeout ${2 * 60 * 1000}
                                --recursive
                                --bail`);
  } finally {
    if (process.env.CI) {
      shellUtils.exec.kill(`Simulator`);
      shellUtils.exec.kill(`CoreSimulator`);
      shellUtils.exec.execSync(`sleep 5`);
    }
    shellUtils.exec.execSync(`echo 'travis_fold:end:detox-ios'`);
  }
}

function installFbsimctlIfNeeded() {
  if (!isInstalled(`fbsimctl`)) {
    shellUtils.exec.execSync(`brew tap facebook/fb && brew install fbsimctl`);
  }
}

function run() {
  installFbsimctlIfNeeded();
  buildProjForDetox();
  e2e();
}

run();