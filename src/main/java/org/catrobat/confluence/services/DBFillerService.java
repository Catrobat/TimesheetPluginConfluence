/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.catrobat.confluence.services;

import com.atlassian.activeobjects.tx.Transactional;

/**
 *
 * @author chri
 */
@Transactional

public interface DBFillerService {
  void cleanDB(); 
  void insertDefaultData();
  void printDBStatus(); 
}
