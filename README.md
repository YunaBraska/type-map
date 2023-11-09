# TypeMap and TypeConverter

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
