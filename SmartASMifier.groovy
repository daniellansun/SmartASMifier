/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


import com.google.googlejavaformat.java.Formatter
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.ASMifier
import org.objectweb.asm.util.TraceClassVisitor

@Grapes([
        @Grab('org.ow2.asm:asm:7.0'),
        @Grab('org.ow2.asm:asm-util:7.0'),
        @Grab('com.google.googlejavaformat:google-java-format:1.5')
])
import javax.tools.JavaCompiler
@Grapes([
        @Grab('org.ow2.asm:asm:7.0'),
        @Grab('org.ow2.asm:asm-util:7.0'),
        @Grab('com.google.googlejavaformat:google-java-format:1.5')
])
import javax.tools.JavaCompiler
import javax.tools.JavaFileObject
import javax.tools.StandardJavaFileManager
import javax.tools.ToolProvider

/**
 * A utility to compile Java source code to ASM source code(or bytecode as text)
 *
 * @author <a href="mailto:realbluesun@hotmail.com">Daniel.Sun</a>
 * Created on 2017/12/19
 */
class SmartASMifier {
    private SmartASMifier() {}

    static String asmify(boolean showBytecode, String... paths) {
        paths.each { path ->
            File javaSrcFile = new File(compileJava(path).canonicalPath) // Create a new File instance to avoid `getParentFile()` returning null
            File javaSrcDir = javaSrcFile.getParentFile()
            String javaSrcFileName = javaSrcFile.name

            List<File> classFiles = javaSrcDir.listFiles().grep { f ->
                if (f.isDirectory()) {
                    return false
                }

                String javaSrcFileNameWithoutExt = javaSrcFileName.replaceAll(/(.+?)[.]java$/, '$1')
                f.name ==~ /${javaSrcFileNameWithoutExt}([$].+)?[.]class/
            }

            classFiles.each { classFile ->
                try {
                    String result
                    String asmSrc = "// ${classFile.canonicalPath}\n${compileClass(showBytecode, classFile)}"

                    if (showBytecode) {
                        result = asmSrc
                    } else {
                        result = new Formatter().formatSource(asmSrc)
                    }

                    println result
                } finally {
                    classFile.delete()
                }
            }
        }
    }

    private static File compileJava(String path) {
        File javaSrcFile = new File(path)

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler()
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)
        try {
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(javaSrcFile))
            compiler.getTask(null, fileManager, null, null, null, compilationUnits).call()
        } finally {
            fileManager.close()
        }

        return javaSrcFile
    }

    private static String compileClass(boolean showBytecode, File file) {
        def sw = new StringWriter()
        new ClassReader(file.bytes).accept(showBytecode ? new TraceClassVisitor(new PrintWriter(sw)) : new TraceClassVisitor(null, new ASMifier(), new PrintWriter(sw)), 0)

        return sw.toString()
    }

    public static final String BYTECODE_OPT = '-b'

    static void main(String[] args) {
        if (args.size() == 0) {
            println "Usage:\n./asmify.sh [-b] <the paths of java source files>\nShow ASM source code:\n./asmify.sh jsrc/HelloWorld.java jsrc/HelloWorld2.java\nShow bytecode:\n./asmify.sh -b jsrc/HelloWorld.java jsrc/HelloWorld2.java"
            System.exit(1)
        }

        asmify(args.contains(BYTECODE_OPT), args.grep { it != BYTECODE_OPT } as String[])
    }
}
