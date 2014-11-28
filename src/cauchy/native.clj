(ns cauchy.native
  (:import [java.io File FileOutputStream IOException InputStream OutputStream FileNotFoundException]
           [clojure.lang RT])
  (:require [clojure.string :as str]))

;; this is a clojure port of http://frommyplayground.com/how-to-load-native-jni-library-from-jar/
;; thanks !!

(defn load-native-from-jar
  [^String path]

  ;; (when-not (.startsWith path "/")
  ;;   (throw (IllegalArgumentException. "The path has to be absolute (start with '/').")))

  (let [parts (str/split path #"\/")
        filename  (when (> (count parts) 1)
                    (last parts))

        ;; Split filename to prexif and suffix (extension)
        parts (.split filename "\\." 2)
        prefix (first parts)
        suffix (when (> (count parts) 1)
                 (str "." (last parts)))]

    ;; Check if the filename is okay
    (when (or (nil? filename) (< (count prefix) 3))
      (throw (IllegalArgumentException. "The filename has to be at least 3 characters long.")))

    (let [^File temp (File/createTempFile prefix suffix)]
      ;; (.deleteOnExit temp)
      (when (not (.exists temp))
        (throw (FileNotFoundException.
                (str "File " (.getAbsolutePath temp) " does not exist."))))
      (let [;; Prepare buffer for data copying
            buffer (byte-array 1024)
            ^InputStream is (.getResourceAsStream (RT/baseLoader) path)
            ^OutputStream os (FileOutputStream. temp)]

        ;; Check input stream
        (when (nil? is)
          (throw (FileNotFoundException.
                  (str "File " path " was not found in JAR."))))
        ;; do the copy
        (loop []
          (let [size (.read is buffer)]
            (when (pos? size)
              (do (.write os buffer 0 size)
                  (recur))))))
      ;; Finally, load the library
      (println "loadding from" (.getAbsolutePath temp))
      (System/load (.getAbsolutePath temp)))))
