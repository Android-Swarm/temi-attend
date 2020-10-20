package com.zetzaus.temiattend.database

import javax.inject.Inject

class VisitorRepository @Inject constructor(private val dao: VisitorDao) : VisitorDao by dao