# Aplicación legado: Teammates
Esta es la versión actualizada de la aplicación Teammates para el experimento de modernización. Se realizaron cambios en varios archivos, sin embargo, en casi todos los casos se trató de incluir excepciones necesarias por la implementación de peticiones HTTP en el archivo CourseLogic.java. La clase CourseLogic fue la única clase en la que se hicierion cambios significativos en cuanto al funcionamiento de la aplicación, ya que esta se encarga de la comunicación con el nuevo microservicio [Courses](https://github.com/DaMoAndes/Modernizacion_entregafinal). Dentro del archivo CoursesLogic.java se resaltaron los cambios realizados a través de comentarios:

# Setup
El setup del entorno de desarrollo de Teammates es bastante complejo, pero existe una guía con instrucciones detalladas que se pueden encontrar aquí:
[https://teammates.github.io/teammates/setting-up.html]

# Integrantes Grupo 1:
* ZARAY VIVIANA REY VIVIESCAS
* CHRISTIAN BORRÁS TORRES
* DAVID MORALES AGUILAR
* PATRICK MYKODA

A continuación se muestra la documentación de la versión orignal:

# TEAMMATES Developer Web Site

[![GitHub Actions Build Status Component Tests](https://github.com/TEAMMATES/teammates/workflows/Component%20Tests/badge.svg)](https://github.com/TEAMMATES/teammates/actions)
[![GitHub Actions Build Status E2E Tests](https://github.com/TEAMMATES/teammates/workflows/E2E%20Tests/badge.svg)](https://github.com/TEAMMATES/teammates/actions)
[![Codecov Coverage Status](https://codecov.io/gh/TEAMMATES/teammates/branch/master/graph/badge.svg)](https://codecov.io/gh/TEAMMATES/teammates)
[![License](https://img.shields.io/badge/license-GPLv2-blue.svg)](LICENSE)

TEAMMATES is a free online tool for managing peer evaluations and other feedback paths of your students.
It is provided as a cloud-based service for educators/students and is currently used by hundreds of universities across the world.

<img src="src/web/assets/images/overview.png" width="600">

This is the developer web site for TEAMMATES. **Click [here](http://teammatesv4.appspot.com/) to go to the TEAMMATES product website.**

[**Documentation for Developers** :book:](https://teammates.github.io/teammates) |
[Version History](https://github.com/TEAMMATES/teammates/milestones?direction=desc&sort=due_date&state=closed) |
[Project Stats](https://www.openhub.net/p/teammatesonline)

## Interested to join TEAMMATES developer team?

We welcome contributions from developers, especially students. Here are some resources:
* [**Contributor Orientation Guide**](https://teammates.github.io/teammates/contributing-doc.html): This document describes what you need to know/do to become a contributor.
* [**Project ideas page**](https://github.com/TEAMMATES/teammates/wiki): These are for those who would like to do a relatively bigger projects with TEAMMATES (e.g. summer internships).

## Acknowledgements

TEAMMATES team wishes to thank the following invaluable contributions:
* [**School of Computing, National University of Singapore (NUS)**](http://www.comp.nus.edu.sg), for providing us with the infrastructure support to run the project.
* [**Centre for Development of Teaching and Learning (CDTL)**](https://nus.edu.sg/cdtl) of NUS, for supporting us with several *Teaching Enhancement Grants* over the years.
* **Learning Innovation Fund-Technology (LIF-T)** initiative of NUS, for funding us for the 2015-2018 period.
* **Google Summer of Code** Program, for including TEAMMATES as a mentor organization in *GSoC2014*, *GSoC2015*, *GSoC2016*, *GSoC2017* and *GSoC2018* editions.
* **Facebook Open Academy** Program, for including TEAMMATES as a mentor organization in FBOA 2016.
* **Jet Brains**, for the [Intellij IDEA](https://www.jetbrains.com/idea/) licences
* <img src="src/web/assets/images/yklogo.png" width="100"> [**YourKit LLC**](https://www.yourkit.com), for providing us with free licenses for the [YourKit Java Profiler](https://www.yourkit.com/java/profiler) (an industry leading profiler tool for Java applications).
* <img src="src/web/assets/images/saucelabs.png" width="100"> [**SauceLabs**](https://saucelabs.com), for providing us with a free [Open Sauce account](https://saucelabs.com/open-source) for cross-browser testing.

## Contacting us

The best way to contact us is to [post a message in our issue tracker](https://github.com/TEAMMATES/teammates/issues/new). Our issue tracker doubles as a discussion forum. You can use it for things like asking questions about the project or requesting technical help.

Alternatively (less preferred), you can email us at **teammates@comp.nus.edu.sg**.
