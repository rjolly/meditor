<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:mathml="http://www.w3.org/1998/Math/MathML" xmlns:svg="http://www.w3.org/2000/svg" version="1.0">

<xsl:output method="xml" indent="yes"/>

<xsl:template match="xhtml:body">
<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
<fo:layout-master-set>
<fo:simple-page-master master-name="simple" page-height="18.5cm" page-width="12cm" margin-top="1cm" margin-bottom="1cm" margin-left="1cm" margin-right="1cm">
<fo:region-body margin-top=".5cm" margin-bottom=".5cm"/>
</fo:simple-page-master>
</fo:layout-master-set>
<fo:page-sequence master-reference="simple">
<fo:flow flow-name="xsl-region-body">
<xsl:apply-templates/>
</fo:flow>
</fo:page-sequence>
</fo:root>
</xsl:template>

<xsl:template match="xhtml:tt">
<fo:block font-size="11pt" font-family="monospace" linefeed-treatment="preserve">
<xsl:for-each select="*|text()">
	<xsl:choose>
		<xsl:when test="self::text()">
			<xsl:choose>
				<xsl:when test="starts-with(., '&#xA;')">
					<xsl:value-of select="substring-after(., '&#xA;')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="."/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:when>
		<xsl:otherwise>
			<xsl:apply-templates select="."/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:for-each>
</fo:block>
</xsl:template>

<xsl:template match="xhtml:hr">
<fo:block break-before="page"/>
</xsl:template>

<xsl:template match="mathml:math">
<fo:instream-foreign-object>
<xsl:copy-of select="."/>
</fo:instream-foreign-object>
</xsl:template>

<xsl:template match="svg:svg">
<fo:instream-foreign-object>
<xsl:copy-of select="."/>
</fo:instream-foreign-object>
</xsl:template>

<xsl:template match="xhtml:br">
<xsl:text>&#xA;</xsl:text>
</xsl:template>

</xsl:stylesheet>
