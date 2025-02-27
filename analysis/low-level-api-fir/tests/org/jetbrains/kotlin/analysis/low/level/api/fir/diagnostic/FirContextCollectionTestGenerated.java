/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostic;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.analysis.api.GenerateAnalysisApiTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("analysis/low-level-api-fir/testdata/fileStructure")
@TestDataPath("$PROJECT_ROOT")
public class FirContextCollectionTestGenerated extends AbstractFirContextCollectionTest {
    @Test
    public void testAllFilesPresentInFileStructure() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/low-level-api-fir/testdata/fileStructure"), Pattern.compile("^(.+)\\.kt$"), null, true);
    }

    @Test
    @TestMetadata("class.kt")
    public void testClass() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/class.kt");
    }

    @Test
    @TestMetadata("classMemberProperty.kt")
    public void testClassMemberProperty() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/classMemberProperty.kt");
    }

    @Test
    @TestMetadata("danglingAnnotationClassLevel.kt")
    public void testDanglingAnnotationClassLevel() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/danglingAnnotationClassLevel.kt");
    }

    @Test
    @TestMetadata("danglingAnnotationTopLevel.kt")
    public void testDanglingAnnotationTopLevel() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/danglingAnnotationTopLevel.kt");
    }

    @Test
    @TestMetadata("declarationsInPropertyInit.kt")
    public void testDeclarationsInPropertyInit() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/declarationsInPropertyInit.kt");
    }

    @Test
    @TestMetadata("enumClass.kt")
    public void testEnumClass() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/enumClass.kt");
    }

    @Test
    @TestMetadata("enumClassWithBody.kt")
    public void testEnumClassWithBody() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/enumClassWithBody.kt");
    }

    @Test
    @TestMetadata("initBlock.kt")
    public void testInitBlock() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/initBlock.kt");
    }

    @Test
    @TestMetadata("localClass.kt")
    public void testLocalClass() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/localClass.kt");
    }

    @Test
    @TestMetadata("localFun.kt")
    public void testLocalFun() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/localFun.kt");
    }

    @Test
    @TestMetadata("localProperty.kt")
    public void testLocalProperty() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/localProperty.kt");
    }

    @Test
    @TestMetadata("memberTypeAlias.kt")
    public void testMemberTypeAlias() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/memberTypeAlias.kt");
    }

    @Test
    @TestMetadata("nestedClasses.kt")
    public void testNestedClasses() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/nestedClasses.kt");
    }

    @Test
    @TestMetadata("propertyAccessors.kt")
    public void testPropertyAccessors() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/propertyAccessors.kt");
    }

    @Test
    @TestMetadata("topLevelExpressionBodyFunWithType.kt")
    public void testTopLevelExpressionBodyFunWithType() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/topLevelExpressionBodyFunWithType.kt");
    }

    @Test
    @TestMetadata("topLevelExpressionBodyFunWithoutType.kt")
    public void testTopLevelExpressionBodyFunWithoutType() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/topLevelExpressionBodyFunWithoutType.kt");
    }

    @Test
    @TestMetadata("topLevelFunWithType.kt")
    public void testTopLevelFunWithType() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/topLevelFunWithType.kt");
    }

    @Test
    @TestMetadata("topLevelProperty.kt")
    public void testTopLevelProperty() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/topLevelProperty.kt");
    }

    @Test
    @TestMetadata("topLevelUnitFun.kt")
    public void testTopLevelUnitFun() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/topLevelUnitFun.kt");
    }

    @Test
    @TestMetadata("typeAlias.kt")
    public void testTypeAlias() throws Exception {
        runTest("analysis/low-level-api-fir/testdata/fileStructure/typeAlias.kt");
    }
}
