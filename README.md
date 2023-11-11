# TypeMap and TypeConverter

[![Build][build_shield]][build_link]
[![Maintainable][maintainable_shield]][maintainable_link]
[![Coverage][coverage_shield]][coverage_link]
[![Issues][issues_shield]][issues_link]
[![Commit][commit_shield]][commit_link]
[![Dependencies][dependency_shield]][dependency_link]
[![License][license_shield]][license_link]
[![Central][central_shield]][central_link]
[![Tag][tag_shield]][tag_link]
[![Javadoc][javadoc_shield]][javadoc_link]
[![Size][size_shield]][size_shield]
![Label][label_shield]
![Label][java_version]

Efficient type conversion is pivotal in Java development but often comes with the cost of cumbersome boilerplate code,
reflection, and performance constraints, particularly when targeting GraalVM native images. Our library, TypeMap and
TypeConverter, is designed to eliminate these pain points by providing a performant, dynamic, and extensible type
conversion utility that is native-ready for GraalVM, all while maintaining type safety through a functional,
reflection-free approach.

### Motivation

I often needed type conversion within maps.
The common libs i could find were reflection or memory heavy and were hard to understand or to extend. So i wrote this
simple example without any magic happening :)

### Features

- **[TypeMap](src/main/java/berlin/yuna/typemap/model/TypeMap.java)**: Threadsafe map - auto convert values on `get`
  using _[TypeConverter](src/main/java/berlin/yuna/typemap/logic/TypeConverter.java)_
- **[TypeConverter](src/main/java/berlin/yuna/typemap/logic/TypeConverter.java)**: Cross conversion between Classes,
  Arrays, Collections, Maps and Enums using
  _[TypeConversionRegister](src/main/java/berlin/yuna/typemap/config/TypeConversionRegister.java)_
- **[TypeConversionRegister](src/main/java/berlin/yuna/typemap/config/TypeConversionRegister.java)**: Extendable
  register for type conversions
- **Pure Java**: No external dependencies for a lightweight integration.
- **Functional Design**: Embraces modern Java functional paradigms.
- **No Reflection**: Ensures compatibility with GraalVM native images.
- **High Performance**: Tailored for efficiency in intensive workloads.
- **Dynamic & Extendable**: Simplifies adding new type conversions.
- **Type Safety**: Utilizes Java generics for compile-time type checking.

### Installation

Not on maven central yet

### Usage

#### TypeMap Basics

```
TypeMap typeMap = new TypeMap();
typeMap.put("key", "value");
String value = typeMap.get("key", String.class);
```

#### TypeMap Simple Conversions

```
String myTime = new Date().toString();
TypeMap typeMap = new TypeMap();
typeMap.put("mykey", myTime);
Instant instant = typeMap.get("mykey", Instant.class);
LocalTime localTime = typeMap.get("mykey", LocalTime.class);
```

#### TypeMap Collections

```
String myTime = new Date().toString();
TypeMap typeMap = new TypeMap();
typeMap.put("mykey", myTime);
List<Instant> instantList = typeMap.get("mykey", ArrayList::new, Instant.class);
```

```
TypeMap typeMap = new TypeMap();
typeMap.put("mykey", new String[]{"1","2","3"});
List<Integer> ingeterList = typeMap.get("mykey", ArrayList::new, Integer.class);
Float[] floatArray = typeMap.getArray("mykey", Float.class);
```

#### TypeMap Maps

```
TypeMap typeMap = new TypeMap();
typeMap.put("mykey", Map.of(1, new Date()));
Map<Long, Instant> instantMap = typeMap.get("mykey", HashMap::new, Long.class, Instant.class);
```

### TypeConverter

The `TypeConverter` is the core of the `TypeMap` and provides find methods like:

* `TypeConverter.mapOf`
* `TypeConverter.convertObj`
* `TypeConverter.collectionOf`

### Adding Custom Conversions (TypeMap & TypeConverter)

Don't worry, any exception is ignored and results in `null`

```
TypeConversionRegister.registerTypeConvert(Path.class, File.class, Path::toFile);
TypeConversionRegister.registerTypeConvert(Path.class, URI.class, Path::toUri);
TypeConversionRegister.registerTypeConvert(Path.class, URL.class, path -> path.toUri().toURL());
```



[build_shield]: https://github.com/YunaBraska/type-map/workflows/MVN_RELEASE/badge.svg
[build_link]: https://github.com/YunaBraska/type-map/actions?query=workflow%3AMVN_RELEASE
[maintainable_shield]: https://img.shields.io/codeclimate/maintainability/YunaBraska/type-map?style=flat-square
[maintainable_link]: https://codeclimate.com/github/YunaBraska/type-map/maintainability
[coverage_shield]: https://img.shields.io/codeclimate/coverage/YunaBraska/type-map?style=flat-square
[coverage_link]: https://codeclimate.com/github/YunaBraska/type-map/test_coverage
[issues_shield]: https://img.shields.io/github/issues/YunaBraska/type-map?style=flat-square
[issues_link]: https://github.com/YunaBraska/type-map/commits/main
[commit_shield]: https://img.shields.io/github/last-commit/YunaBraska/type-map?style=flat-square
[commit_link]: https://github.com/YunaBraska/type-map/issues
[license_shield]: https://img.shields.io/github/license/YunaBraska/type-map?style=flat-square
[license_link]: https://github.com/YunaBraska/type-map/blob/main/LICENSE
[dependency_shield]: https://img.shields.io/librariesio/github/YunaBraska/type-map?style=flat-square
[dependency_link]: https://libraries.io/github/YunaBraska/type-map
[central_shield]: https://img.shields.io/maven-central/v/berlin.yuna/type-map?style=flat-square
[central_link]:https://search.maven.org/artifact/berlin.yuna/type-map
[tag_shield]: https://img.shields.io/github/v/tag/YunaBraska/type-map?style=flat-square
[tag_link]: https://github.com/YunaBraska/type-map/releases
[javadoc_shield]: https://javadoc.io/badge2/berlin.yuna/type-map/javadoc.svg?style=flat-square
[javadoc_link]: https://javadoc.io/doc/berlin.yuna/type-map
[size_shield]: https://img.shields.io/github/repo-size/YunaBraska/type-map?style=flat-square
[label_shield]: https://img.shields.io/badge/Yuna-QueenInside-blueviolet?style=flat-square
[gitter_shield]: https://img.shields.io/gitter/room/YunaBraska/type-map?style=flat-square
[gitter_link]: https://gitter.im/type-map/Lobby
[java_version]: https://img.shields.io/badge/java-8-blueviolet?style=flat-square
