<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:m="http://www.w3.org/1998/Math/MathML"
		xmlns:x="http://www.w3.org/1999/xhtml"
                version='1.0'>

<xsl:output method="text" indent="no" encoding="UTF-8"/>

<xsl:template match="text()"/>

<xsl:template match="x:a">
	<xsl:call-template name="txt">
		<xsl:with-param name="value" select="@href"/>
	</xsl:call-template>
</xsl:template>

<xsl:template name="txt">
	<xsl:param name="value"/>
	<xsl:if test="not(contains($value, ':')) and not(contains($value, '/'))">
		<xsl:value-of select="$value"/>
		<xsl:text>&#10;</xsl:text>
	</xsl:if>
</xsl:template>

</xsl:stylesheet>