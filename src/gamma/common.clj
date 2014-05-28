(ns gamma.common
  (:import (java.io BufferedReader FileReader InputStreamReader File)
           javax.imageio.ImageIO))


(defn strip-colon
  "Remove the first colon found in the string"
  [s]
  (clojure.string/replace-first s ":" ""))

(defn keyword-append
  "Append two keywords or strings together separated by a colon, and return the result as a keyword"
  [kw-a kw-b]
  (keyword (str (strip-colon (str kw-a)) "-" (strip-colon (str kw-b)))))



(defn resolve-line
  "Helper function for loading properties files.
  Parse the line and add the key -> property association to the map"
  [def-map line]
  (if (clojure.string/blank? line)
    def-map
    (let [tokens (clojure.string/split line #"\s+")]
      (if (> (count tokens) 1)
        (let [kw (read-string (str ":" (first tokens)))
              tokes (map read-string (rest tokens))
              val (if (= 1 (count tokes)) (first tokes) tokes) ]
          (assoc def-map kw val))
        def-map))))


(defn load-property-file
  [filename]
  (assert (not (nil? filename)) "Property filename not defined")
  (let [load-class (.getClass (Thread/currentThread))
        istream (.getResourceAsStream load-class (str "/" filename))
        _ (assert (not (nil? istream)) (str "Could not open stream for " filename)) ]
    (with-open [rdr (BufferedReader. (InputStreamReader. istream))]
      (reduce resolve-line {} (line-seq rdr)))))

(def ^:dynamic *common-properties* (atom nil))
(def common-property-file "common.txt")

(defn load-common-properties
  []
  (reset! *common-properties* (load-property-file common-property-file)))

(defn get-property
  [kw]
  (@*common-properties* kw))


;======================================================================================
;


;/**
;   * List directory contents for a resource folder. Not recursive.
;   * This is basically a brute-force implementation.
;   * Works for regular files and also JARs.
;   * 
;   * @author Greg Briggs
;   * @param clazz Any java class that lives in the same place as the resources you want.
;   * @param path Should end with "/", but not start with one.
;   * @return Just the name of each member item, not the full paths.
;   * @throws URISyntaxException 
;   * @throws IOException 
;   */
;  String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
;      URL dirURL = clazz.getClassLoader().getResource(path);
;      if (dirURL != null && dirURL.getProtocol().equals("file")) {
;        /* A file path: easy enough */
;        return new File(dirURL.toURI()).list();
;      } 
;
;      if (dirURL == null) {
;        /* 
;         * In case of a jar file, we can't actually find a directory.
;         * Have to assume the same jar as clazz.
;         */
;        String me = clazz.getName().replace(".", "/")+".class";
;        dirURL = clazz.getClassLoader().getResource(me);
;      }
;      
;      if (dirURL.getProtocol().equals("jar")) {
;        /* A JAR path */
;        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
;        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
;        Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
;        Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
;        while(entries.hasMoreElements()) {
;          String name = entries.nextElement().getName();
;          if (name.startsWith(path)) { //filter according to the path
;            String entry = name.substring(path.length());
;            int checkSubdir = entry.indexOf("/");
;            if (checkSubdir >= 0) {
;              // if it is a subdirectory, we just return the directory name
;              entry = entry.substring(0, checkSubdir);
;            }
;            result.add(entry);
;          }
;        }
;        return result.toArray(new String[result.size()]);
;      } 
;        
;      throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
;  }
;


(defn get-resource-list
  [path]
  (let [load-class (.getClass (Thread/currentThread))
        alternate-class ""
        dir-url (or (.getResource load-class path)
                    (.getResource (.getClassLoader load-class) alternate-class )) ]
    (case (.getProtocol dir-url)
      "file" 
      (.list (File. (.toURI dir-url)))
      "jar"
      ;need to complete this part
      '())  
    ))
(defn main
  []
  (prn (load-property-file common-property-file))
  )

