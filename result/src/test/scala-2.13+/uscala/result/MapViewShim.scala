package uscala.result

object MapViewShim {
  implicit class MapViewImplicitShim[K, V](map: Map[K, V]) {
    def mapValuesShim[W](f: (V) => W): Map[K, W] = map.view.mapValues(f).toMap
  }
}
