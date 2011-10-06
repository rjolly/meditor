<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:m="http://www.w3.org/1998/Math/MathML"
                version='1.0'>
                
<xsl:output method="text" indent="no" encoding="UTF-8"/>

<xsl:strip-space elements="m:*"/>

<xsl:template match="m:math">
	<xsl:text>&#x00022;</xsl:text>
	<xsl:apply-templates/>
	<xsl:text>&#x00022;</xsl:text>
</xsl:template>

</xsl:stylesheet>