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

@Grapes([
        @Grab('org.ow2.asm:asm:6.0'),
        @Grab('org.ow2.asm:asm-util:6.0'),
        @Grab('com.google.googlejavaformat:google-java-format:1.5')
])

import javax.tools.JavaCompiler
import javax.tools.JavaFileObject
import javax.tools.ToolProvider
import javax.tools.StandardJavaFileManager
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.ASMifier
import org.objectweb.asm.util.TraceClassVisitor
import com.google.googlejavaformat.java.Formatter

/**
 * A utility to compile Java source code to ASM source code
 *
 * @author <a href="mailto:realbluesun@hotmail.com">Daniel.Sun</a>
 * Created on 2017/12/19
 */
public class SmartASMifier {
    private SmartASMifier() {}

    public static String asmify(String... paths) {
        paths.each { path ->
            File javaSrcFile = compileJava(path)

            File javaSrcDir = javaSrcFile.getParentFile()
            String javaSrcFileName = javaSrcFile.name

            List<File> classFiles = javaSrcDir.listFiles().grep { f ->
            		String javaSrcFileNameWithoutExt = javaSrcFileName.replaceAll(/(.+?)[.]java$/, '$1')
                f.name ==~ /${javaSrcFileNameWithoutExt}([$].+)?[.]class/
            }

            classFiles.each { classFile ->
                try {
                    String asmSrc = "// ${classFile.canonicalPath}\n${compileClass(classFile)}"
                    println new Formatter().formatSource(asmSrc);
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
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(javaSrcFile));
            compiler.getTask(null, fileManager, null, null, null, compilationUnits).call()
        } finally {
            fileManager.close()
        }

        return javaSrcFile
    }

    private static String compileClass(File file) {
        def sw = new StringWriter()
        new ClassReader(file.bytes).accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(sw)), 0)

        return sw.toString()
    }

    public static void main(String[] args) {
        if (args.size() == 0) {
            println "Usage: ./asmify.sh <the paths of java source files>\nFor example: ./asmify.sh jsrc/HelloWorld.java jsrc/HelloWorld2.java"
            System.exit(1)
        }

        SmartASMifier.asmify(args)
    }
}

