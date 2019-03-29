# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
### Changed
### Deprecated
### Removed
### Fixed
### Security

## [1.5.1] - 2019-03-29
### Fixed
- Exclude old dependencies of Tools-1.5.

## [1.5] - 2017-05-29
### Added
- Support full text search (METS only)

### Changed
- Support for KIT Data Manager 1.5 (This version is not backward compatible)
- Ingest multiple directories in parallel

## [1.4.2] - 2017-02-03  
### Fixed
- Test for REST credentials was broken with KIT DM 1.4.                                           

## [1.4.1] - 2017-02-03
### Added
- Support for plugins extending ingest client.
- Example plugin (incl. code)
- Drag'n drop client for windows.
- Webdav settings work with KIT Data Manager 1.4+
- Improve documentation

### Changed
- Support for ingesting multiple directories at once.    

### Fixed  
- Investigation may be generated during setup.


## [1.4] - 2016-07-21
### Added
- Define interfaces for external code using inversion-of-control-pattern.
- Add sample code to documentation

### Fixed   
- Test for webdav access point now also test for write access


## [1.3] - 2016-06-21
### Added
- Possibility to use multiple configurations in parallel 
- Use environment variable for configuration file (REPO_SETTINGS)

### Changed
- Rename GenericRestClient -> GenericRepoClient
- KIT Data Manager libraries 1.3

### Fixed  
- Download also possible with KIT DM 1.2+  


## [1.2] - 2015-09-02
### Added
- Settings now stored per account.

### Changed
- KIT Data Manager libraries 1.1


## [1.1] - 2015-03-11 
### Changed
- new commands (setupRepo & repoClient)

## [1.0] - 2015-01-23
### Added
- Initial Release
